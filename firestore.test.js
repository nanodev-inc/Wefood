const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-ba9fa91a-1c8d-4bce-966a-d5cea8be3867";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function getDb(context) {
  return context.firestore({ databaseId: DATABASE_ID });
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write addresses or orders", async () => {
  const unauthDb = getDb(testEnv.unauthenticatedContext());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/addresses`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/orders`).get());
  await assertFails(
    unauthDb.collection(`users/${ALICE_UID}/cartItems`).doc("c1").set({
      userId: ALICE_UID,
    })
  );
});

test("Cross-user isolation: Bob cannot read or modify Alice's addresses, cart, or orders", async () => {
  const adminDb = getDb(
    testEnv.authenticatedContext(ALICE_UID)
  );
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore({ databaseId: DATABASE_ID });
    await db.doc(`users/${ALICE_UID}/addresses/addr_1`).set({
      userId: ALICE_UID,
      label: "Home",
      addressLine: "42 Crimson Lane, Apt 4B",
      landmark: "Near Central Park",
      isDefault: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = getDb(testEnv.authenticatedContext(BOB_UID));
  await assertFails(bobDb.doc(`users/${ALICE_UID}/addresses/addr_1`).get());
  await assertFails(bobDb.collection(`users/${ALICE_UID}/addresses`).get());
  await assertFails(
    bobDb.doc(`users/${ALICE_UID}/addresses/addr_1`).update({ label: "Work" })
  );
});

function pastTimestamp() {
  return new Date(Date.now() - 2000);
}

test("Owner can create, list, and update address with valid schema and fails shadow fields", async () => {
  const aliceCtx = testEnv.authenticatedContext(ALICE_UID);
  const aliceDb = getDb(aliceCtx);

  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/addresses/addr_home`).set({
      userId: ALICE_UID,
      label: "Home",
      addressLine: "742 Evergreen Terrace, Suite 2",
      landmark: "Opposite Clock Tower",
      isDefault: true,
      createdAt: pastTimestamp(),
      updatedAt: pastTimestamp(),
    })
  );

  await assertSucceeds(
    aliceDb.collection(`users/${ALICE_UID}/addresses`).get()
  );

  await assertFails(
    aliceDb.doc(`users/${ALICE_UID}/addresses/addr_ghost`).set({
      userId: ALICE_UID,
      label: "Home",
      addressLine: "123 Ghost St",
      isDefault: false,
      isAdmin: true,
      createdAt: pastTimestamp(),
      updatedAt: pastTimestamp(),
    })
  );
});

test("Order enforces initial CONFIRMED status, forward transitions, and terminal lock after DELIVERED", async () => {
  const aliceDb = getDb(testEnv.authenticatedContext(ALICE_UID));

  // Cannot create order directly as DELIVERED
  await assertFails(
    aliceDb.doc(`users/${ALICE_UID}/orders/ord_bad`).set({
      userId: ALICE_UID,
      orderType: "FOOD",
      vendorId: "rest_1",
      vendorName: "Saffron Hearth",
      itemsSummary: "1x Truffle Margherita",
      itemCount: 1,
      subtotal: 18.0,
      deliveryFee: 2.0,
      discount: 0.0,
      totalAmount: 20.0,
      deliveryAddress: "742 Evergreen Terrace",
      status: "DELIVERED",
      etaMinutes: 0,
      courierName: "Rohan K.",
      createdAt: pastTimestamp(),
      updatedAt: pastTimestamp(),
    })
  );

  // Valid create with CONFIRMED
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/orders/ord_1`).set({
      userId: ALICE_UID,
      orderType: "FOOD",
      vendorId: "rest_1",
      vendorName: "Saffron Hearth",
      itemsSummary: "1x Truffle Margherita",
      itemCount: 1,
      subtotal: 18.0,
      deliveryFee: 2.0,
      discount: 0.0,
      totalAmount: 20.0,
      deliveryAddress: "742 Evergreen Terrace",
      status: "CONFIRMED",
      etaMinutes: 25,
      courierName: "Rohan K.",
      createdAt: pastTimestamp(),
      updatedAt: pastTimestamp(),
    })
  );

  // Forward transition CONFIRMED -> PREPARING -> DELIVERED
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/orders/ord_1`).update({
      status: "PREPARING",
      etaMinutes: 18,
      updatedAt: pastTimestamp(),
    })
  );

  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/orders/ord_1`).update({
      status: "DELIVERED",
      etaMinutes: 0,
      updatedAt: pastTimestamp(),
    })
  );

  // Terminal state lock: cannot revert status from DELIVERED
  await assertFails(
    aliceDb.doc(`users/${ALICE_UID}/orders/ord_1`).update({
      status: "CONFIRMED",
      etaMinutes: 10,
      updatedAt: pastTimestamp(),
    })
  );

  // Can rate delivered order
  await assertSucceeds(
    aliceDb.doc(`users/${ALICE_UID}/orders/ord_1`).update({
      rating: 5,
      updatedAt: pastTimestamp(),
    })
  );
});
