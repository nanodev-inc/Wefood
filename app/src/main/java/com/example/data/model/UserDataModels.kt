package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class OrderStatus(val wireValue: String, val label: String, val stepIndex: Int) {
  CONFIRMED("CONFIRMED", "Order Confirmed", 0),
  PREPARING("PREPARING", "Preparing & Packing", 1),
  OUT_FOR_DELIVERY("OUT_FOR_DELIVERY", "Out for Delivery", 2),
  DELIVERED("DELIVERED", "Delivered", 3);

  fun nextStage(): OrderStatus? = when (this) {
    CONFIRMED -> PREPARING
    PREPARING -> OUT_FOR_DELIVERY
    OUT_FOR_DELIVERY -> DELIVERED
    DELIVERED -> null
  }

  companion object {
    fun fromWire(value: String): OrderStatus =
      entries.firstOrNull { it.wireValue == value } ?: CONFIRMED
  }
}

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String = "",
  val activeAddressId: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class SavedAddress(
  @DocumentId val id: String = "",
  val userId: String = "",
  val label: String = "Home",
  val addressLine: String = "",
  val landmark: String = "",
  val isDefault: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class FavoriteRestaurant(
  @DocumentId val id: String = "",
  val userId: String = "",
  val restaurantId: String = "",
  val restaurantName: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class CartEntry(
  @DocumentId val id: String = "",
  val userId: String = "",
  val sourceType: String = SourceType.FOOD.wireValue,
  val vendorId: String = "",
  val vendorName: String = "",
  val itemId: String = "",
  val itemName: String = "",
  val unitPrice: Double = 0.0,
  val quantity: Long = 1L,
  val isVeg: Boolean = true,
  val optionsSummary: String = "",
  val specialNote: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class FoodOrder(
  @DocumentId val id: String = "",
  val userId: String = "",
  val orderType: String = SourceType.FOOD.wireValue,
  val vendorId: String = "",
  val vendorName: String = "",
  val itemsSummary: String = "",
  val itemCount: Long = 0L,
  val subtotal: Double = 0.0,
  val deliveryFee: Double = 0.0,
  val discount: Double = 0.0,
  val totalAmount: Double = 0.0,
  val deliveryAddress: String = "",
  val couponCode: String = "",
  val status: String = OrderStatus.CONFIRMED.wireValue,
  val etaMinutes: Long = 20L,
  val courierName: String = "",
  val rating: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)
