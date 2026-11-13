package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.OrderStatus
import com.example.data.model.Restaurant
import com.example.data.repository.CatalogRepository
import com.example.data.repository.UserDataRepository
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOutUser
import com.example.ui.components.AddressPickerBottomSheet
import com.example.ui.components.CartVendorConflictDialog
import com.example.ui.components.FloatingCartBar
import com.example.ui.components.LiveOrderMiniBanner
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartCheckoutScreen
import com.example.ui.screens.FoodTabScreen
import com.example.ui.screens.MartTabScreen
import com.example.ui.screens.OrdersTabScreen
import com.example.ui.screens.RestaurantDetailScreen
import com.example.ui.screens.SavedTabScreen
import com.example.ui.theme.CraveCartTheme
import com.example.ui.viewmodel.CraveCartViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

enum class MainTab {
  FOOD,
  MART,
  ORDERS,
  SAVED,
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      CraveCartTheme {
        AppNavigation()
      }
    }
  }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = {},
      onUnauthenticated = {},
      scope = scope,
    )
  }

  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    AuthScreen(
      onAuthSuccess = {},
    )
  } else {
    key(user.uid) {
      CraveCartAppShell(
        currentUserId = user.uid,
        displayName = user.displayName ?: "CraveCart Member",
        email = user.email ?: "",
        onSignOut = {
          signOutUser(credentialManager = credentialManager, scope = scope)
        },
      )
    }
  }
}

@Composable
fun CraveCartAppShell(
  currentUserId: String,
  displayName: String,
  email: String,
  onSignOut: () -> Unit,
) {
  val viewModel: CraveCartViewModel = viewModel(
    key = currentUserId,
    factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) {
          "APPLICATION_KEY missing from CreationExtras"
        }
        val databaseId = app.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(databaseId)
        CraveCartViewModel(
          repository = UserDataRepository(db),
          currentUserId = currentUserId,
          displayName = displayName,
          email = email,
        )
      }
    },
  )

  val foodFilter by viewModel.foodFilter.collectAsStateWithLifecycle()
  val martFilter by viewModel.martFilter.collectAsStateWithLifecycle()
  val restaurants by viewModel.filteredRestaurants.collectAsStateWithLifecycle()
  val groceryItems by viewModel.filteredGroceryItems.collectAsStateWithLifecycle()
  val addresses by viewModel.addresses.collectAsStateWithLifecycle()
  val activeAddress by viewModel.activeAddress.collectAsStateWithLifecycle()
  val favoriteIds by viewModel.favoriteRestaurantIds.collectAsStateWithLifecycle()
  val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
  val pricing by viewModel.cartPricing.collectAsStateWithLifecycle()
  val ordersState by viewModel.ordersState.collectAsStateWithLifecycle()
  val pendingReplace by viewModel.pendingCartReplace.collectAsStateWithLifecycle()
  val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
  val isPlacingOrder by viewModel.isPlacingOrder.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableStateOf(MainTab.FOOD) }
  var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
  var showCartCheckout by remember { mutableStateOf(false) }
  var showAddressSheet by remember { mutableStateOf(false) }

  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(toastMessage) {
    toastMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearToast()
    }
  }

  val activeOrders = (ordersState as? UiState.Success)?.data
    ?.filter { OrderStatus.fromWire(it.status) != OrderStatus.DELIVERED }
    .orEmpty()
  val primaryLiveOrder = activeOrders.firstOrNull()

  val showFloatingCart = !showCartCheckout && cartItems.isNotEmpty() && selectedTab != MainTab.ORDERS
  val showLiveBanner = !showCartCheckout && primaryLiveOrder != null && selectedTab != MainTab.ORDERS

  val bottomOverlayDp = when {
    showFloatingCart && showLiveBanner -> 136.dp
    showFloatingCart || showLiveBanner -> 78.dp
    else -> 12.dp
  }

  val navItems = listOf(
    Triple(MainTab.FOOD, stringResource(R.string.nav_food), Pair(Icons.Filled.Restaurant, Icons.Outlined.Restaurant)),
    Triple(MainTab.MART, stringResource(R.string.nav_mart), Pair(Icons.Filled.Bolt, Icons.Outlined.Bolt)),
    Triple(MainTab.ORDERS, stringResource(R.string.nav_orders), Pair(Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong)),
    Triple(MainTab.SAVED, stringResource(R.string.nav_saved), Pair(Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)),
  )

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useRail = maxWidth >= 680.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (useRail && !showCartCheckout) {
        NavigationRail(
          containerColor = MaterialTheme.colorScheme.surface,
          modifier = Modifier
            .fillMaxHeight()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
          Spacer(modifier = Modifier.height(12.dp))
          navItems.forEach { (tab, label, icons) ->
            val selected = selectedTab == tab && selectedRestaurant == null
            NavigationRailItem(
              selected = selected,
              onClick = {
                selectedRestaurant = null
                selectedTab = tab
              },
              icon = {
                Icon(
                  imageVector = if (selected) icons.first else icons.second,
                  contentDescription = label,
                )
              },
              label = { Text(label) },
            )
          }
        }
      }

      Scaffold(
        modifier = Modifier.weight(1f),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
          if (!useRail && !showCartCheckout) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              windowInsets = WindowInsets.navigationBars,
            ) {
              navItems.forEach { (tab, label, icons) ->
                val selected = selectedTab == tab
                NavigationBarItem(
                  selected = selected,
                  onClick = {
                    selectedRestaurant = null
                    selectedTab = tab
                  },
                  icon = {
                    Icon(
                      imageVector = if (selected) icons.first else icons.second,
                      contentDescription = label,
                    )
                  },
                  label = { Text(label) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                  ),
                  modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                )
              }
            }
          }
        },
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
          contentAlignment = Alignment.TopCenter,
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .widthIn(max = 960.dp),
          ) {
            when {
              showCartCheckout -> {
                CartCheckoutScreen(
                  cartItems = cartItems,
                  pricing = pricing,
                  activeAddress = activeAddress,
                  isPlacingOrder = isPlacingOrder,
                  onBack = { showCartCheckout = false },
                  onUpdateQuantity = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                  onClearCart = { viewModel.clearCart() },
                  onApplyCoupon = { code -> viewModel.applyCoupon(code) },
                  onRemoveCoupon = { viewModel.removeCoupon() },
                  onChangeAddress = { showAddressSheet = true },
                  onPlaceOrder = {
                    viewModel.placeOrder {
                      showCartCheckout = false
                      selectedRestaurant = null
                      selectedTab = MainTab.ORDERS
                    }
                  },
                )
              }

              selectedRestaurant != null -> {
                val rest = selectedRestaurant!!
                RestaurantDetailScreen(
                  restaurant = rest,
                  isFavorite = favoriteIds.contains(rest.id),
                  bottomOverlayPadding = bottomOverlayDp,
                  onBack = { selectedRestaurant = null },
                  onToggleFavorite = { viewModel.toggleFavorite(rest) },
                  onAddCustomizedDish = { dish, unitPrice, qty, summary, note ->
                    viewModel.addCustomizedDishToCart(rest, dish, unitPrice, qty, summary, note)
                  },
                )
              }

              selectedTab == MainTab.FOOD -> {
                FoodTabScreen(
                  activeAddress = activeAddress,
                  filterState = foodFilter,
                  restaurants = restaurants,
                  favoriteIds = favoriteIds,
                  bottomOverlayPadding = bottomOverlayDp,
                  onOpenAddressPicker = { showAddressSheet = true },
                  onSearchChange = viewModel::updateFoodSearch,
                  onSelectCuisine = viewModel::selectCuisine,
                  onTogglePureVeg = viewModel::togglePureVeg,
                  onToggleFast = viewModel::toggleUnder25Mins,
                  onToggleTopRated = viewModel::toggleTopRated,
                  onToggleOffers = viewModel::toggleOffers,
                  onClearFilters = viewModel::clearFoodFilters,
                  onSwitchToMart = { selectedTab = MainTab.MART },
                  onRestaurantClick = { selectedRestaurant = it },
                  onToggleFavorite = viewModel::toggleFavorite,
                )
              }

              selectedTab == MainTab.MART -> {
                MartTabScreen(
                  activeAddress = activeAddress,
                  filterState = martFilter,
                  groceryItems = groceryItems,
                  cartItems = cartItems,
                  bottomOverlayPadding = bottomOverlayDp,
                  onOpenAddressPicker = { showAddressSheet = true },
                  onSearchChange = viewModel::updateMartSearch,
                  onSelectCategory = viewModel::selectMartCategory,
                  onIncrementItem = viewModel::incrementMartItem,
                  onDecrementItem = viewModel::decrementMartItem,
                )
              }

              selectedTab == MainTab.ORDERS -> {
                OrdersTabScreen(
                  ordersState = ordersState,
                  bottomOverlayPadding = 16.dp,
                  onExploreFood = { selectedTab = MainTab.FOOD },
                  onAdvanceOrderStage = viewModel::advanceOrderManually,
                  onReorder = { order ->
                    viewModel.reorderPastOrder(order) {
                      showCartCheckout = true
                    }
                  },
                  onRateOrder = viewModel::rateOrder,
                )
              }

              selectedTab == MainTab.SAVED -> {
                val favRestaurants = CatalogRepository.restaurants.filter { favoriteIds.contains(it.id) }
                SavedTabScreen(
                  displayName = displayName,
                  email = email,
                  addresses = addresses,
                  activeAddressId = activeAddress?.id,
                  favoriteRestaurants = favRestaurants,
                  bottomOverlayPadding = bottomOverlayDp,
                  onOpenAddressPicker = { showAddressSheet = true },
                  onSelectAddress = viewModel::selectAddress,
                  onOpenRestaurant = { rest ->
                    selectedRestaurant = rest
                  },
                  onSignOut = onSignOut,
                )
              }
            }

            Column(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              if (showLiveBanner) {
                primaryLiveOrder?.let { liveOrder ->
                  LiveOrderMiniBanner(
                    order = liveOrder,
                    onClick = {
                      selectedRestaurant = null
                      selectedTab = MainTab.ORDERS
                    },
                  )
                }
              }

              FloatingCartBar(
                visible = showFloatingCart,
                itemCount = pricing.itemCount,
                vendorName = cartItems.firstOrNull()?.vendorName ?: "",
                subtotal = pricing.subtotal,
                onClick = { showCartCheckout = true },
              )
            }
          }
        }
      }
    }
  }

  if (showAddressSheet) {
    AddressPickerBottomSheet(
      addresses = addresses,
      activeAddressId = activeAddress?.id,
      onSelectAddress = viewModel::selectAddress,
      onAddNewAddress = viewModel::addAddress,
      onDeleteAddress = viewModel::deleteAddress,
      onDismiss = { showAddressSheet = false },
    )
  }

  pendingReplace?.let { conflict ->
    CartVendorConflictDialog(
      currentVendor = conflict.currentVendorName,
      newVendor = conflict.newVendorName,
      onReplace = viewModel::confirmReplaceCart,
      onCancel = viewModel::dismissReplaceCart,
    )
  }
}
