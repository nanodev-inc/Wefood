package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CartEntry
import com.example.data.model.FavoriteRestaurant
import com.example.data.model.FoodOrder
import com.example.data.model.OrderStatus
import com.example.data.model.SavedAddress
import com.example.data.model.SourceType
import com.example.data.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserDataRepository(private val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  fun observeUserProfile(userId: String): Flow<UserProfile?> {
    val docRef = db.collection("users").document(userId)
    return docRef.snapshots()
      .map { snap ->
        snap.toObject(
          UserProfile::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        )
      }
      .catch { error ->
        rethrowOrLog(error, OperationType.GET, docRef.path)
      }
  }

  fun observeAddresses(userId: String): Flow<List<SavedAddress>> {
    val colRef = db.collection("users").document(userId).collection("addresses")
    return colRef.snapshots()
      .map { snap ->
        snap.toObjects(
          SavedAddress::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ).sortedByDescending { it.isDefault }
      }
      .catch { error ->
        rethrowOrLog(error, OperationType.LIST, colRef.path)
      }
  }

  fun observeFavorites(userId: String): Flow<List<FavoriteRestaurant>> {
    val colRef = db.collection("users").document(userId).collection("favorites")
    return colRef.snapshots()
      .map { snap ->
        snap.toObjects(
          FavoriteRestaurant::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        )
      }
      .catch { error ->
        rethrowOrLog(error, OperationType.LIST, colRef.path)
      }
  }

  fun observeCartItems(userId: String): Flow<List<CartEntry>> {
    val colRef = db.collection("users").document(userId).collection("cartItems")
    return colRef.snapshots()
      .map { snap ->
        snap.toObjects(
          CartEntry::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        ).sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
      }
      .catch { error ->
        rethrowOrLog(error, OperationType.LIST, colRef.path)
      }
  }

  fun observeOrders(userId: String): Flow<List<FoodOrder>> {
    val colRef = db.collection("users").document(userId).collection("orders")
    return colRef.orderBy("createdAt", Query.Direction.DESCENDING)
      .snapshots()
      .map { snap ->
        snap.toObjects(
          FoodOrder::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
        )
      }
      .catch { error ->
        rethrowOrLog(error, OperationType.LIST, colRef.path)
      }
  }

  suspend fun ensureUserProfileAndDefaultAddress(
    userId: String,
    displayName: String,
    email: String,
  ): Result<Unit> {
    val safeName = displayName.trim().ifBlank { "Foodie" }.take(80)
    val safeEmail = email.trim().take(120)
    val userDoc = db.collection("users").document(userId)

    return try {
      val snapshot = userDoc.get().await()
      if (!snapshot.exists()) {
        val defaultAddrRef = userDoc.collection("addresses").document("home_default")
        val batch = db.batch()
        batch.set(
          userDoc,
          mapOf(
            "userId" to userId,
            "displayName" to safeName,
            "email" to safeEmail,
            "activeAddressId" to defaultAddrRef.id,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        batch.set(
          defaultAddrRef,
          mapOf(
            "userId" to userId,
            "label" to "Home",
            "addressLine" to "42 Crimson Avenue, Apt 4B",
            "landmark" to "Near Maple Arts Square",
            "isDefault" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        batch.commit().await()
      } else {
        userDoc.update(
          mapOf(
            "displayName" to safeName,
            "email" to safeEmail,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, userDoc.path)
      Result.failure(e)
    }
  }

  suspend fun addAddress(
    userId: String,
    label: String,
    addressLine: String,
    landmark: String,
    makeActive: Boolean = true,
  ): Result<String> {
    val cleanLabel = if (label in listOf("Home", "Work", "Other")) label else "Other"
    val cleanAddress = addressLine.trim().take(180)
    val cleanLandmark = landmark.trim().take(120)
    val addrId = "addr_${UUID.randomUUID().toString().replace("-", "").take(16)}"
    val addrRef = db.collection("users").document(userId).collection("addresses").document(addrId)

    return try {
      val payload = mutableMapOf<String, Any>(
        "userId" to userId,
        "label" to cleanLabel,
        "addressLine" to cleanAddress,
        "isDefault" to makeActive,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      if (cleanLandmark.isNotEmpty()) {
        payload["landmark"] = cleanLandmark
      }
      addrRef.set(payload).await()
      if (makeActive) {
        setActiveAddress(userId, addrId)
      }
      Result.success(addrId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, addrRef.path)
      Result.failure(e)
    }
  }

  suspend fun setActiveAddress(userId: String, addressId: String): Result<Unit> {
    val userRef = db.collection("users").document(userId)
    return try {
      userRef.update(
        mapOf(
          "activeAddressId" to addressId,
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, userRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteAddress(userId: String, addressId: String): Result<Unit> {
    val addrRef = db.collection("users").document(userId).collection("addresses").document(addressId)
    return try {
      addrRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, addrRef.path)
      Result.failure(e)
    }
  }

  suspend fun toggleFavorite(
    userId: String,
    restaurantId: String,
    restaurantName: String,
    currentlyFavorite: Boolean,
  ): Result<Unit> {
    val favRef = db.collection("users").document(userId).collection("favorites").document(restaurantId)
    return try {
      if (currentlyFavorite) {
        favRef.delete().await()
      } else {
        favRef.set(
          mapOf(
            "userId" to userId,
            "restaurantId" to restaurantId,
            "restaurantName" to restaurantName.take(100),
            "createdAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, if (currentlyFavorite) OperationType.DELETE else OperationType.CREATE, favRef.path)
      Result.failure(e)
    }
  }

  suspend fun addToCart(
    userId: String,
    sourceType: SourceType,
    vendorId: String,
    vendorName: String,
    itemId: String,
    itemName: String,
    unitPrice: Double,
    quantity: Int,
    isVeg: Boolean,
    optionsSummary: String = "",
    specialNote: String = "",
    replaceIncompatibleCart: Boolean = false,
  ): Result<String> {
    val cartCol = db.collection("users").document(userId).collection("cartItems")
    return try {
      val existingDocs = cartCol.get().await().documents
      if (replaceIncompatibleCart && existingDocs.isNotEmpty()) {
        val clearBatch = db.batch()
        existingDocs.forEach { clearBatch.delete(it.reference) }
        clearBatch.commit().await()
      }

      // Match existing line item if same itemId and identical customization options
      val normalizedOptions = optionsSummary.trim().take(240)
      val normalizedNote = specialNote.trim().take(200)
      val matchingDoc = if (!replaceIncompatibleCart) {
        existingDocs.firstOrNull { doc ->
          doc.getString("itemId") == itemId &&
            (doc.getString("optionsSummary") ?: "") == normalizedOptions &&
            (doc.getString("specialNote") ?: "") == normalizedNote
        }
      } else {
        null
      }

      if (matchingDoc != null) {
        val currentQty = matchingDoc.getLong("quantity") ?: 1L
        val updatedQty = (currentQty + quantity).coerceIn(1L, 50L)
        matchingDoc.reference.update(
          mapOf(
            "quantity" to updatedQty,
            "unitPrice" to unitPrice,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
        Result.success(matchingDoc.id)
      } else {
        val entryId = "cart_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = cartCol.document(entryId)
        val payload = mutableMapOf<String, Any>(
          "userId" to userId,
          "sourceType" to sourceType.wireValue,
          "vendorId" to vendorId,
          "vendorName" to vendorName.take(100),
          "itemId" to itemId,
          "itemName" to itemName.take(120),
          "unitPrice" to unitPrice,
          "quantity" to quantity.coerceIn(1, 50),
          "isVeg" to isVeg,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
        if (normalizedOptions.isNotEmpty()) {
          payload["optionsSummary"] = normalizedOptions
        }
        if (normalizedNote.isNotEmpty()) {
          payload["specialNote"] = normalizedNote
        }
        docRef.set(payload).await()
        Result.success(entryId)
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, cartCol.path)
      Result.failure(e)
    }
  }

  suspend fun updateCartItemQuantity(
    userId: String,
    cartItemId: String,
    newQuantity: Int,
  ): Result<Unit> {
    val docRef = db.collection("users").document(userId).collection("cartItems").document(cartItemId)
    return try {
      if (newQuantity <= 0) {
        docRef.delete().await()
      } else {
        docRef.update(
          mapOf(
            "quantity" to newQuantity.coerceIn(1, 50),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun clearCart(userId: String): Result<Unit> {
    val cartCol = db.collection("users").document(userId).collection("cartItems")
    return try {
      val docs = cartCol.get().await().documents
      if (docs.isNotEmpty()) {
        val batch = db.batch()
        docs.forEach { batch.delete(it.reference) }
        batch.commit().await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, cartCol.path)
      Result.failure(e)
    }
  }

  suspend fun placeOrder(
    userId: String,
    orderType: SourceType,
    vendorId: String,
    vendorName: String,
    itemsSummary: String,
    itemCount: Int,
    subtotal: Double,
    deliveryFee: Double,
    discount: Double,
    totalAmount: Double,
    deliveryAddress: String,
    couponCode: String,
    etaMinutes: Int,
    courierName: String,
  ): Result<String> {
    val userDoc = db.collection("users").document(userId)
    val orderId = "ord_${UUID.randomUUID().toString().replace("-", "").take(16)}"
    val orderRef = userDoc.collection("orders").document(orderId)

    return try {
      val cartDocs = userDoc.collection("cartItems").get().await().documents
      val batch = db.batch()
      val payload = mutableMapOf<String, Any>(
        "userId" to userId,
        "orderType" to orderType.wireValue,
        "vendorId" to vendorId,
        "vendorName" to vendorName.take(100),
        "itemsSummary" to itemsSummary.take(500),
        "itemCount" to itemCount.coerceIn(1, 100),
        "subtotal" to subtotal.coerceAtLeast(0.0),
        "deliveryFee" to deliveryFee.coerceAtLeast(0.0),
        "discount" to discount.coerceAtLeast(0.0),
        "totalAmount" to totalAmount.coerceAtLeast(0.0),
        "deliveryAddress" to deliveryAddress.take(240),
        "status" to OrderStatus.CONFIRMED.wireValue,
        "etaMinutes" to etaMinutes.coerceIn(1, 180),
        "courierName" to courierName.take(80),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
      if (couponCode.isNotBlank()) {
        payload["couponCode"] = couponCode.trim().take(32)
      }
      batch.set(orderRef, payload)
      cartDocs.forEach { batch.delete(it.reference) }
      batch.commit().await()
      Result.success(orderId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, orderRef.path)
      Result.failure(e)
    }
  }

  suspend fun advanceOrderStatus(
    userId: String,
    orderId: String,
    nextStatus: OrderStatus,
    nextEtaMinutes: Int,
  ): Result<Unit> {
    val orderRef = db.collection("users").document(userId).collection("orders").document(orderId)
    return try {
      orderRef.update(
        mapOf(
          "status" to nextStatus.wireValue,
          "etaMinutes" to nextEtaMinutes.coerceIn(0, 180),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, orderRef.path)
      Result.failure(e)
    }
  }

  suspend fun rateOrder(userId: String, orderId: String, rating: Int): Result<Unit> {
    val orderRef = db.collection("users").document(userId).collection("orders").document(orderId)
    return try {
      orderRef.update(
        mapOf(
          "rating" to rating.coerceIn(1, 5),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      ).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, orderRef.path)
      Result.failure(e)
    }
  }

  private fun rethrowOrLog(error: Throwable, operationType: OperationType, path: String): Nothing {
    val firestoreError = generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    handleFirestoreError(firestoreError ?: error, operationType, path)
    throw error
  }
}
