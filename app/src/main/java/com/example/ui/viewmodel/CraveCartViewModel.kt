package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartEntry
import com.example.data.model.FavoriteRestaurant
import com.example.data.model.FoodOrder
import com.example.data.model.GroceryItem
import com.example.data.model.MenuItem
import com.example.data.model.OrderStatus
import com.example.data.model.PromoCoupon
import com.example.data.model.Restaurant
import com.example.data.model.SavedAddress
import com.example.data.model.SourceType
import com.example.data.model.UserProfile
import com.example.data.repository.CatalogRepository
import com.example.data.repository.UserDataRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

data class FoodFilterState(
  val searchQuery: String = "",
  val selectedCuisine: String = "All",
  val pureVegOnly: Boolean = false,
  val under25MinsOnly: Boolean = false,
  val topRatedOnly: Boolean = false,
  val offersOnly: Boolean = false,
)

data class MartFilterState(
  val searchQuery: String = "",
  val selectedCategoryId: String? = null,
)

data class PendingCartReplace(
  val currentVendorName: String,
  val newVendorName: String,
  val sourceType: SourceType,
  val vendorId: String,
  val itemId: String,
  val itemName: String,
  val unitPrice: Double,
  val quantity: Int,
  val isVeg: Boolean,
  val optionsSummary: String,
  val specialNote: String,
)

data class CartPricingSummary(
  val itemCount: Int = 0,
  val subtotal: Double = 0.0,
  val deliveryFee: Double = 0.0,
  val discount: Double = 0.0,
  val taxesAndCharges: Double = 0.0,
  val grandTotal: Double = 0.0,
  val appliedCoupon: PromoCoupon? = null,
)

class CraveCartViewModel(
  private val repository: UserDataRepository,
  private val currentUserId: String,
  private val displayName: String,
  private val email: String,
) : ViewModel() {

  private val _foodFilter = MutableStateFlow(FoodFilterState())
  val foodFilter: StateFlow<FoodFilterState> = _foodFilter.asStateFlow()

  private val _martFilter = MutableStateFlow(MartFilterState())
  val martFilter: StateFlow<MartFilterState> = _martFilter.asStateFlow()

  private val _appliedCouponCode = MutableStateFlow<String?>(null)
  val appliedCouponCode: StateFlow<String?> = _appliedCouponCode.asStateFlow()

  private val _pendingCartReplace = MutableStateFlow<PendingCartReplace?>(null)
  val pendingCartReplace: StateFlow<PendingCartReplace?> = _pendingCartReplace.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  private val _isPlacingOrder = MutableStateFlow(false)
  val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

  val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
    .catch { error ->
      val fsError = unwrapFirestoreError(error)
      if (fsError == null && error is CancellationException) throw error
      Log.w(TAG, "Error observing profile", fsError ?: error)
      emit(null)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

  val addresses: StateFlow<List<SavedAddress>> = repository.observeAddresses(currentUserId)
    .catch { error ->
      val fsError = unwrapFirestoreError(error)
      if (fsError == null && error is CancellationException) throw error
      Log.w(TAG, "Error observing addresses", fsError ?: error)
      emit(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

  val activeAddress: StateFlow<SavedAddress?> = combine(userProfile, addresses) { profile, list ->
    val targetId = profile?.activeAddressId.orEmpty()
    list.firstOrNull { it.id == targetId } ?: list.firstOrNull { it.isDefault } ?: list.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

  val favoriteRestaurantIds: StateFlow<Set<String>> = repository.observeFavorites(currentUserId)
    .map { list -> list.map(FavoriteRestaurant::restaurantId).toSet() }
    .catch { error ->
      val fsError = unwrapFirestoreError(error)
      if (fsError == null && error is CancellationException) throw error
      Log.w(TAG, "Error observing favorites", fsError ?: error)
      emit(emptySet())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptySet())

  val cartItems: StateFlow<List<CartEntry>> = repository.observeCartItems(currentUserId)
    .catch { error ->
      val fsError = unwrapFirestoreError(error)
      if (fsError == null && error is CancellationException) throw error
      Log.w(TAG, "Error observing cart", fsError ?: error)
      emit(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

  val ordersState: StateFlow<UiState<List<FoodOrder>>> = repository.observeOrders(currentUserId)
    .map<List<FoodOrder>, UiState<List<FoodOrder>>> { UiState.Success(it) }
    .catch { error ->
      val fsError = unwrapFirestoreError(error)
      if (fsError == null && error is CancellationException) throw error
      val root = fsError ?: error
      Log.w(TAG, "Error observing orders", root)
      emit(UiState.Error(root.message ?: "Unable to sync your orders"))
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

  val filteredRestaurants: StateFlow<List<Restaurant>> = _foodFilter
    .map { filter ->
      val q = filter.searchQuery.trim().lowercase()
      CatalogRepository.restaurants.filter { r ->
        val matchesQuery = q.isEmpty() ||
          r.name.lowercase().contains(q) ||
          r.cuisines.any { it.lowercase().contains(q) } ||
          r.tagline.lowercase().contains(q) ||
          CatalogRepository.getMenuForRestaurant(r.id).any { it.name.lowercase().contains(q) }
        val matchesCuisine = filter.selectedCuisine == "All" ||
          r.cuisines.any { it.equals(filter.selectedCuisine, ignoreCase = true) }
        val matchesVeg = !filter.pureVegOnly || r.isPureVeg
        val matchesFast = !filter.under25MinsOnly || r.etaMinutes <= 25
        val matchesRating = !filter.topRatedOnly || r.rating >= 4.6
        val matchesOffer = !filter.offersOnly || !r.promoTag.isNullOrBlank()
        matchesQuery && matchesCuisine && matchesVeg && matchesFast && matchesRating && matchesOffer
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CatalogRepository.restaurants)

  val filteredGroceryItems: StateFlow<List<GroceryItem>> = _martFilter
    .map { filter ->
      val q = filter.searchQuery.trim().lowercase()
      CatalogRepository.groceryItems.filter { item ->
        val matchesQuery = q.isEmpty() || item.name.lowercase().contains(q)
        val matchesCat = filter.selectedCategoryId == null || item.categoryId == filter.selectedCategoryId
        matchesQuery && matchesCat
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CatalogRepository.groceryItems)

  val cartPricing: StateFlow<CartPricingSummary> = combine(cartItems, _appliedCouponCode) { items, code ->
    calculatePricing(items, code)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CartPricingSummary())

  init {
    viewModelScope.launch {
      repository.ensureUserProfileAndDefaultAddress(currentUserId, displayName, email)
    }
    startLiveOrderProgressionLoop()
  }

  private fun startLiveOrderProgressionLoop() {
    viewModelScope.launch {
      while (true) {
        delay(ORDER_STEP_INTERVAL_MS)
        val currentOrders = (ordersState.value as? UiState.Success)?.data.orEmpty()
        val activeOrders = currentOrders.filter {
          OrderStatus.fromWire(it.status) != OrderStatus.DELIVERED
        }
        for (order in activeOrders) {
          val currentStatus = OrderStatus.fromWire(order.status)
          val nextStatus = currentStatus.nextStage() ?: continue
          val nextEta = when (nextStatus) {
            OrderStatus.CONFIRMED -> order.etaMinutes.toInt()
            OrderStatus.PREPARING -> if (order.orderType == SourceType.MART.wireValue) 7 else 16
            OrderStatus.OUT_FOR_DELIVERY -> if (order.orderType == SourceType.MART.wireValue) 3 else 7
            OrderStatus.DELIVERED -> 0
          }
          repository.advanceOrderStatus(currentUserId, order.id, nextStatus, nextEta)
        }
      }
    }
  }

  fun advanceOrderManually(order: FoodOrder) {
    val currentStatus = OrderStatus.fromWire(order.status)
    val nextStatus = currentStatus.nextStage() ?: return
    val nextEta = when (nextStatus) {
      OrderStatus.CONFIRMED -> order.etaMinutes.toInt()
      OrderStatus.PREPARING -> if (order.orderType == SourceType.MART.wireValue) 6 else 15
      OrderStatus.OUT_FOR_DELIVERY -> if (order.orderType == SourceType.MART.wireValue) 2 else 6
      OrderStatus.DELIVERED -> 0
    }
    viewModelScope.launch {
      repository.advanceOrderStatus(currentUserId, order.id, nextStatus, nextEta)
    }
  }

  fun updateFoodSearch(query: String) {
    _foodFilter.value = _foodFilter.value.copy(searchQuery = query)
  }

  fun selectCuisine(cuisine: String) {
    _foodFilter.value = _foodFilter.value.copy(selectedCuisine = cuisine)
  }

  fun togglePureVeg() {
    _foodFilter.value = _foodFilter.value.copy(pureVegOnly = !_foodFilter.value.pureVegOnly)
  }

  fun toggleUnder25Mins() {
    _foodFilter.value = _foodFilter.value.copy(under25MinsOnly = !_foodFilter.value.under25MinsOnly)
  }

  fun toggleTopRated() {
    _foodFilter.value = _foodFilter.value.copy(topRatedOnly = !_foodFilter.value.topRatedOnly)
  }

  fun toggleOffers() {
    _foodFilter.value = _foodFilter.value.copy(offersOnly = !_foodFilter.value.offersOnly)
  }

  fun clearFoodFilters() {
    _foodFilter.value = FoodFilterState()
  }

  fun updateMartSearch(query: String) {
    _martFilter.value = _martFilter.value.copy(searchQuery = query)
  }

  fun selectMartCategory(categoryId: String?) {
    val next = if (_martFilter.value.selectedCategoryId == categoryId) null else categoryId
    _martFilter.value = _martFilter.value.copy(selectedCategoryId = next)
  }

  fun toggleFavorite(restaurant: Restaurant) {
    val isFav = favoriteRestaurantIds.value.contains(restaurant.id)
    viewModelScope.launch {
      repository.toggleFavorite(currentUserId, restaurant.id, restaurant.name, isFav)
      showToast(if (isFav) "Removed ${restaurant.name} from favorites" else "Saved ${restaurant.name} to favorites")
    }
  }

  fun addCustomizedDishToCart(
    restaurant: Restaurant,
    dish: MenuItem,
    unitPrice: Double,
    quantity: Int,
    optionsSummary: String,
    specialNote: String,
  ) {
    addItemWithVendorGuard(
      sourceType = SourceType.FOOD,
      vendorId = restaurant.id,
      vendorName = restaurant.name,
      itemId = dish.id,
      itemName = dish.name,
      unitPrice = unitPrice,
      quantity = quantity,
      isVeg = dish.isVeg,
      optionsSummary = optionsSummary,
      specialNote = specialNote,
    )
  }

  fun addMartItemToCart(item: GroceryItem) {
    addItemWithVendorGuard(
      sourceType = SourceType.MART,
      vendorId = CatalogRepository.MART_VENDOR_ID,
      vendorName = CatalogRepository.MART_VENDOR_NAME,
      itemId = item.id,
      itemName = item.name,
      unitPrice = item.price,
      quantity = 1,
      isVeg = item.isVeg,
      optionsSummary = item.unitLabel,
      specialNote = "",
    )
  }

  fun incrementMartItem(item: GroceryItem) {
    val existing = cartItems.value.firstOrNull { it.itemId == item.id && it.sourceType == SourceType.MART.wireValue }
    if (existing != null) {
      updateCartQuantity(existing.id, (existing.quantity + 1).toInt())
    } else {
      addMartItemToCart(item)
    }
  }

  fun decrementMartItem(item: GroceryItem) {
    val existing = cartItems.value.firstOrNull { it.itemId == item.id && it.sourceType == SourceType.MART.wireValue }
    if (existing != null) {
      updateCartQuantity(existing.id, (existing.quantity - 1).toInt())
    }
  }

  private fun addItemWithVendorGuard(
    sourceType: SourceType,
    vendorId: String,
    vendorName: String,
    itemId: String,
    itemName: String,
    unitPrice: Double,
    quantity: Int,
    isVeg: Boolean,
    optionsSummary: String,
    specialNote: String,
  ) {
    val currentCart = cartItems.value
    val existingVendorId = currentCart.firstOrNull()?.vendorId
    val existingVendorName = currentCart.firstOrNull()?.vendorName ?: ""

    if (existingVendorId != null && existingVendorId != vendorId) {
      _pendingCartReplace.value = PendingCartReplace(
        currentVendorName = existingVendorName,
        newVendorName = vendorName,
        sourceType = sourceType,
        vendorId = vendorId,
        itemId = itemId,
        itemName = itemName,
        unitPrice = unitPrice,
        quantity = quantity,
        isVeg = isVeg,
        optionsSummary = optionsSummary,
        specialNote = specialNote,
      )
      return
    }

    viewModelScope.launch {
      val result = repository.addToCart(
        userId = currentUserId,
        sourceType = sourceType,
        vendorId = vendorId,
        vendorName = vendorName,
        itemId = itemId,
        itemName = itemName,
        unitPrice = unitPrice,
        quantity = quantity,
        isVeg = isVeg,
        optionsSummary = optionsSummary,
        specialNote = specialNote,
      )
      if (result.isSuccess) {
        showToast("Added $itemName to cart")
      } else {
        showToast("Could not add item. Please try again.")
      }
    }
  }

  fun confirmReplaceCart() {
    val pending = _pendingCartReplace.value ?: return
    _pendingCartReplace.value = null
    _appliedCouponCode.value = null
    viewModelScope.launch {
      val result = repository.addToCart(
        userId = currentUserId,
        sourceType = pending.sourceType,
        vendorId = pending.vendorId,
        vendorName = pending.newVendorName,
        itemId = pending.itemId,
        itemName = pending.itemName,
        unitPrice = pending.unitPrice,
        quantity = pending.quantity,
        isVeg = pending.isVeg,
        optionsSummary = pending.optionsSummary,
        specialNote = pending.specialNote,
        replaceIncompatibleCart = true,
      )
      if (result.isSuccess) {
        showToast("Started fresh cart from ${pending.newVendorName}")
      }
    }
  }

  fun dismissReplaceCart() {
    _pendingCartReplace.value = null
  }

  fun updateCartQuantity(cartItemId: String, newQuantity: Int) {
    viewModelScope.launch {
      repository.updateCartItemQuantity(currentUserId, cartItemId, newQuantity)
    }
  }

  fun clearCart() {
    _appliedCouponCode.value = null
    viewModelScope.launch {
      repository.clearCart(currentUserId)
    }
  }

  fun applyCoupon(code: String) {
    val items = cartItems.value
    if (items.isEmpty()) {
      showToast("Add items before applying a promo code")
      return
    }
    val coupon = CatalogRepository.coupons.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }
    if (coupon == null) {
      showToast("Promo code not recognized")
      return
    }
    val sourceType = SourceType.fromWire(items.first().sourceType)
    if (coupon.applicableType != null && coupon.applicableType != sourceType) {
      showToast("${coupon.code} is valid on ${coupon.applicableType.name.lowercase()} orders only")
      return
    }
    val subtotal = items.sumOf { it.unitPrice * it.quantity }
    if (subtotal < coupon.minOrder) {
      showToast("Add $${String.format("%.2f", coupon.minOrder - subtotal)} more for ${coupon.code}")
      return
    }
    _appliedCouponCode.value = coupon.code
    showToast("Applied ${coupon.code}!")
  }

  fun removeCoupon() {
    _appliedCouponCode.value = null
  }

  fun selectAddress(addressId: String) {
    viewModelScope.launch {
      repository.setActiveAddress(currentUserId, addressId)
      showToast("Delivery address updated")
    }
  }

  fun addAddress(label: String, addressLine: String, landmark: String) {
    if (addressLine.trim().length < 5) {
      showToast("Please enter at least 5 characters for the street address")
      return
    }
    viewModelScope.launch {
      val result = repository.addAddress(currentUserId, label, addressLine, landmark, makeActive = true)
      if (result.isSuccess) {
        showToast("Saved $label address")
      } else {
        showToast("Could not save address")
      }
    }
  }

  fun deleteAddress(addressId: String) {
    viewModelScope.launch {
      repository.deleteAddress(currentUserId, addressId)
      showToast("Address removed")
    }
  }

  fun placeOrder(onOrderPlaced: (String) -> Unit) {
    val items = cartItems.value
    val address = activeAddress.value
    if (items.isEmpty()) {
      showToast("Your cart is empty")
      return
    }
    if (address == null) {
      showToast("Please add a delivery address first")
      return
    }
    if (_isPlacingOrder.value) return

    _isPlacingOrder.value = true
    val pricing = cartPricing.value
    val first = items.first()
    val orderType = SourceType.fromWire(first.sourceType)
    val summary = items.joinToString(", ") { entry ->
      val opt = if (entry.optionsSummary.isNotBlank()) " (${entry.optionsSummary})" else ""
      "${entry.quantity}x ${entry.itemName}$opt"
    }
    val totalCount = items.sumOf { it.quantity }.toInt()
    val eta = if (orderType == SourceType.MART) 10 else {
      CatalogRepository.getRestaurantById(first.vendorId)?.etaMinutes ?: 24
    }
    val courier = listOf("Rohan K.", "Maya S.", "Arjun P.", "Elena V.").random()
    val formattedAddress = buildString {
      append(address.label)
      append(" • ")
      append(address.addressLine)
      if (address.landmark.isNotBlank()) {
        append(" (")
        append(address.landmark)
        append(")")
      }
    }

    viewModelScope.launch {
      val result = repository.placeOrder(
        userId = currentUserId,
        orderType = orderType,
        vendorId = first.vendorId,
        vendorName = first.vendorName,
        itemsSummary = summary,
        itemCount = totalCount,
        subtotal = pricing.subtotal,
        deliveryFee = pricing.deliveryFee,
        discount = pricing.discount,
        totalAmount = pricing.grandTotal,
        deliveryAddress = formattedAddress,
        couponCode = pricing.appliedCoupon?.code.orEmpty(),
        etaMinutes = eta,
        courierName = courier,
      )
      _isPlacingOrder.value = false
      result.onSuccess { orderId ->
        _appliedCouponCode.value = null
        showToast("Order confirmed! Tracking live now.")
        onOrderPlaced(orderId)
      }.onFailure {
        showToast("Could not place order. Check connection and try again.")
      }
    }
  }

  fun reorderPastOrder(order: FoodOrder, onReady: () -> Unit) {
    val firstItemName = order.itemsSummary.substringAfter("x ").substringBefore(",").trim().ifBlank {
      order.vendorName + " Combo"
    }
    val safeUnitPrice = (order.subtotal / order.itemCount.coerceAtLeast(1L)).coerceAtLeast(1.0)
    viewModelScope.launch {
      repository.addToCart(
        userId = currentUserId,
        sourceType = SourceType.fromWire(order.orderType),
        vendorId = order.vendorId,
        vendorName = order.vendorName,
        itemId = "reorder_${order.id.take(12)}",
        itemName = firstItemName,
        unitPrice = safeUnitPrice,
        quantity = 1,
        isVeg = true,
        optionsSummary = "Reordered from past order",
        replaceIncompatibleCart = true,
      ).onSuccess {
        showToast("Added reorder items to your cart")
        onReady()
      }
    }
  }

  fun rateOrder(orderId: String, stars: Int) {
    viewModelScope.launch {
      repository.rateOrder(currentUserId, orderId, stars)
      showToast("Thanks for rating $stars stars!")
    }
  }

  fun showToast(message: String) {
    _toastMessage.value = message
  }

  fun clearToast() {
    _toastMessage.value = null
  }

  private fun calculatePricing(items: List<CartEntry>, couponCode: String?): CartPricingSummary {
    if (items.isEmpty()) return CartPricingSummary()
    val first = items.first()
    val sourceType = SourceType.fromWire(first.sourceType)
    val itemCount = items.sumOf { it.quantity }.toInt()
    val subtotal = items.sumOf { it.unitPrice * it.quantity }

    val baseDeliveryFee = if (sourceType == SourceType.MART) {
      if (subtotal >= 15.0) 0.0 else 1.49
    } else {
      CatalogRepository.getRestaurantById(first.vendorId)?.deliveryFee ?: 1.99
    }

    val validCoupon = couponCode?.let { code ->
      CatalogRepository.coupons.firstOrNull {
        it.code.equals(code, ignoreCase = true) &&
          subtotal >= it.minOrder &&
          (it.applicableType == null || it.applicableType == sourceType)
      }
    }

    val deliveryFee = if (validCoupon?.freeDelivery == true) 0.0 else baseDeliveryFee
    val discount = (validCoupon?.discountAmount ?: 0.0).coerceAtMost(subtotal)
    val taxes = (subtotal - discount).coerceAtLeast(0.0) * 0.05
    val grandTotal = (subtotal - discount + deliveryFee + taxes).coerceAtLeast(0.0)

    return CartPricingSummary(
      itemCount = itemCount,
      subtotal = subtotal,
      deliveryFee = deliveryFee,
      discount = discount,
      taxesAndCharges = taxes,
      grandTotal = grandTotal,
      appliedCoupon = validCoupon,
    )
  }

  private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? =
    generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()

  private companion object {
    const val TAG = "CraveCartVM"
    const val STOP_TIMEOUT_MS = 5000L
    const val ORDER_STEP_INTERVAL_MS = 10000L
  }
}
