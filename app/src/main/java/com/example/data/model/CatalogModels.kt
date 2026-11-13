package com.example.data.model

enum class SourceType(val wireValue: String) {
  FOOD("FOOD"),
  MART("MART");

  companion object {
    fun fromWire(value: String): SourceType =
      entries.firstOrNull { it.wireValue == value } ?: FOOD
  }
}

enum class HeroArtwork {
  FOOD_FEAST,
  FEATURED_BOWL,
  MART_BASKET,
}

data class CustomizationOption(
  val id: String,
  val name: String,
  val extraPrice: Double,
  val isVeg: Boolean = true,
)

data class CustomizationGroup(
  val id: String,
  val title: String,
  val isRequired: Boolean,
  val maxSelection: Int, // 1 = single radio choice, >1 = multi-select checkboxes
  val options: List<CustomizationOption>,
)

data class MenuItem(
  val id: String,
  val restaurantId: String,
  val category: String,
  val name: String,
  val description: String,
  val basePrice: Double,
  val isVeg: Boolean,
  val isBestseller: Boolean = false,
  val rating: Double = 4.6,
  val customizationGroups: List<CustomizationGroup> = emptyList(),
)

data class Restaurant(
  val id: String,
  val name: String,
  val cuisines: List<String>,
  val area: String,
  val rating: Double,
  val reviewCount: String,
  val etaMinutes: Int,
  val deliveryFee: Double,
  val costForTwo: Int,
  val isPureVeg: Boolean,
  val promoTag: String?,
  val artwork: HeroArtwork,
  val accentHex: Long,
  val tagline: String,
)

data class GroceryCategory(
  val id: String,
  val name: String,
  val subtitle: String,
  val badge: String,
  val accentHex: Long,
)

data class GroceryItem(
  val id: String,
  val categoryId: String,
  val name: String,
  val unitLabel: String,
  val price: Double,
  val mrp: Double,
  val discountBadge: String?,
  val isVeg: Boolean = true,
  val isFlashDeal: Boolean = false,
)

data class PromoCoupon(
  val code: String,
  val title: String,
  val subtitle: String,
  val minOrder: Double,
  val discountAmount: Double,
  val freeDelivery: Boolean = false,
  val applicableType: SourceType? = null, // null means both Food and Mart
)
