package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.OrderStatus
import com.example.data.model.SourceType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class UserDataRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun ensureProfileAndAddAddress_ownerSucceedsAndObserves() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = UserDataRepository(firestore)

    val profileRes = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.ensureUserProfileAndDefaultAddress(uid, "Alice Foodie", ALICE_EMAIL)
    }
    assertTrue(profileRes.isSuccess)

    val addrRes = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.addAddress(uid, "Work", "100 Market Plaza Suite 12", "Opposite Fountain", makeActive = true)
    }
    assertTrue(addrRes.isSuccess)

    val addresses = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeAddresses(uid).first { list -> list.any { it.label == "Work" } }
    }
    assertTrue(addresses.any { it.addressLine.contains("100 Market Plaza") })
  }

  @Test
  fun cartAndOrderLifecycle_ownerCreatesAdvancesAndRatesOrder() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = UserDataRepository(firestore)
    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.ensureUserProfileAndDefaultAddress(uid, "Alice Foodie", ALICE_EMAIL)
    }

    val cartRes = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.addToCart(
        userId = uid,
        sourceType = SourceType.FOOD,
        vendorId = "rest_saffron_hearth",
        vendorName = "Saffron Hearth",
        itemId = "dish_royal_biryani",
        itemName = "Royal Saffron Dum Biryani",
        unitPrice = 16.00,
        quantity = 2,
        isVeg = false,
        optionsSummary = "Medium Heat • Mint Raita",
        specialNote = "Less oil",
      )
    }
    assertTrue(cartRes.isSuccess)

    val orderRes = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.placeOrder(
        userId = uid,
        orderType = SourceType.FOOD,
        vendorId = "rest_saffron_hearth",
        vendorName = "Saffron Hearth",
        itemsSummary = "2x Royal Saffron Dum Biryani",
        itemCount = 2,
        subtotal = 32.00,
        deliveryFee = 1.99,
        discount = 5.00,
        totalAmount = 28.99,
        deliveryAddress = "42 Crimson Avenue, Apt 4B",
        couponCode = "CRAVE20",
        etaMinutes = 24,
        courierName = "Arjun M.",
      )
    }
    assertTrue(orderRes.isSuccess)
    val orderId = orderRes.getOrThrow()

    val ordersAfterCreate = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeOrders(uid).first { list -> list.any { it.id == orderId } }
    }
    assertEquals(OrderStatus.CONFIRMED.wireValue, ordersAfterCreate.first { it.id == orderId }.status)

    // Advance CONFIRMED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED
    assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.advanceOrderStatus(uid, orderId, OrderStatus.PREPARING, 16)
    }.isSuccess)
    assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.advanceOrderStatus(uid, orderId, OrderStatus.DELIVERED, 0)
    }.isSuccess)

    // Rating delivered order succeeds
    assertTrue(withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.rateOrder(uid, orderId, 5)
    }.isSuccess)

    // Terminal lock prevents reverting DELIVERED back to CONFIRMED
    val revertRes = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.advanceOrderStatus(uid, orderId, OrderStatus.CONFIRMED, 20)
    }
    assertTrue(revertRes.isFailure)
    val fsErr = revertRes.exceptionOrNull()?.unwrapFirestoreException()
    assertNotNull(fsErr)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsErr?.code)
  }

  @Test
  fun crossUserAccess_bobCannotReadAliceOrders() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repo = UserDataRepository(firestore)
    withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.ensureUserProfileAndDefaultAddress(aliceUid, "Alice", ALICE_EMAIL)
    }

    signInTestUser(BOB_EMAIL)
    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeAddresses(aliceUid).first()
      }
      fail("Expected PERMISSION_DENIED when Bob observes Alice's addresses")
    } catch (e: Exception) {
      val fsErr = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsErr)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsErr?.code)
    }
  }

  @Test
  fun unauthenticatedCaller_observeOrdersFailsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = UserDataRepository(firestore)
    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeOrders("some_user_123").first()
      }
      fail("Expected PERMISSION_DENIED when unauthenticated")
    } catch (e: Exception) {
      val fsErr = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsErr)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsErr?.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice_crave@test.com"
    const val BOB_EMAIL = "bob_crave@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
