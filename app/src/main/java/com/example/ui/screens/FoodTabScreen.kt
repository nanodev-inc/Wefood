package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Restaurant
import com.example.data.model.SavedAddress
import com.example.data.repository.CatalogRepository
import com.example.ui.components.HeroArtworkImage
import com.example.ui.components.VegIndicatorBadge
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.viewmodel.FoodFilterState

@Composable
fun FoodTabScreen(
  activeAddress: SavedAddress?,
  filterState: FoodFilterState,
  restaurants: List<Restaurant>,
  favoriteIds: Set<String>,
  bottomOverlayPadding: androidx.compose.ui.unit.Dp,
  onOpenAddressPicker: () -> Unit,
  onSearchChange: (String) -> Unit,
  onSelectCuisine: (String) -> Unit,
  onTogglePureVeg: () -> Unit,
  onToggleFast: () -> Unit,
  onToggleTopRated: () -> Unit,
  onToggleOffers: () -> Unit,
  onClearFilters: () -> Unit,
  onSwitchToMart: () -> Unit,
  onRestaurantClick: (Restaurant) -> Unit,
  onToggleFavorite: (Restaurant) -> Unit,
) {
  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isTwoColumn = maxWidth >= 680.dp

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = bottomOverlayPadding + 20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        FoodHeaderBar(
          activeAddress = activeAddress,
          onOpenAddressPicker = onOpenAddressPicker,
          onSwitchToMart = onSwitchToMart,
        )
      }

      item {
        OutlinedTextField(
          value = filterState.searchQuery,
          onValueChange = onSearchChange,
          placeholder = { Text(stringResource(R.string.search_food_hint)) },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search food") },
          trailingIcon = {
            if (filterState.searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchChange("") }) {
                Icon(Icons.Filled.Close, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("food_search_input"),
        )
      }

      item {
        EditorialFoodHeroBanner(
          onExploreFeatured = {
            CatalogRepository.restaurants.firstOrNull()?.let(onRestaurantClick)
          },
        )
      }

      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "What are you craving?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(CatalogRepository.cuisineStories) { cuisine ->
              val selected = filterState.selectedCuisine == cuisine
              Surface(
                onClick = { onSelectCuisine(cuisine) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.minimumInteractiveComponentSize(),
              ) {
                Text(
                  text = cuisine,
                  style = MaterialTheme.typography.labelLarge,
                  color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
              }
            }
          }
        }
      }

      item {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          item {
            FilterChip(
              selected = filterState.pureVegOnly,
              onClick = onTogglePureVeg,
              label = { Text("Pure Veg") },
              leadingIcon = { VegIndicatorBadge(isVeg = true) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
              ),
              modifier = Modifier.testTag("filter_pure_veg"),
            )
          }
          item {
            FilterChip(
              selected = filterState.under25MinsOnly,
              onClick = onToggleFast,
              label = { Text("Under 25 min") },
              leadingIcon = {
                Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
              },
              modifier = Modifier.testTag("filter_fast_delivery"),
            )
          }
          item {
            FilterChip(
              selected = filterState.topRatedOnly,
              onClick = onToggleTopRated,
              label = { Text("Rated 4.6+") },
              leadingIcon = {
                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
              },
            )
          }
          item {
            FilterChip(
              selected = filterState.offersOnly,
              onClick = onToggleOffers,
              label = { Text("Great Offers") },
              leadingIcon = {
                Icon(Icons.Filled.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
              },
            )
          }
        }
      }

      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "${restaurants.size} Kitchens Delivering Near You",
            style = MaterialTheme.typography.headlineMedium,
          )
        }
      }

      if (restaurants.isEmpty()) {
        item {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Icon(
                imageVector = Icons.Filled.RestaurantMenu,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text("No restaurants match those filters", style = MaterialTheme.typography.titleMedium)
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                "Reset filters to discover all kitchens delivering around you.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(modifier = Modifier.height(12.dp))
              OutlinedButton(
                onClick = onClearFilters,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
              ) {
                Text("Reset Filters")
              }
            }
          }
        }
      } else if (isTwoColumn) {
        val rows = restaurants.chunked(2)
        items(rows) { pair ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            pair.forEach { restaurant ->
              RestaurantCard(
                restaurant = restaurant,
                isFavorite = favoriteIds.contains(restaurant.id),
                onClick = { onRestaurantClick(restaurant) },
                onToggleFavorite = { onToggleFavorite(restaurant) },
                modifier = Modifier.weight(1f),
              )
            }
            if (pair.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      } else {
        items(restaurants, key = { it.id }) { restaurant ->
          RestaurantCard(
            restaurant = restaurant,
            isFavorite = favoriteIds.contains(restaurant.id),
            onClick = { onRestaurantClick(restaurant) },
            onToggleFavorite = { onToggleFavorite(restaurant) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun FoodHeaderBar(
  activeAddress: SavedAddress?,
  onOpenAddressPicker: () -> Unit,
  onSwitchToMart: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .weight(1f)
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onOpenAddressPicker)
        .padding(vertical = 6.dp, horizontal = 4.dp)
        .testTag("location_picker_button"),
    ) {
      Icon(
        imageVector = Icons.Filled.LocationOn,
        contentDescription = "Delivery location",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(26.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = activeAddress?.label ?: "Choose Address",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = "Switch address",
            modifier = Modifier.size(18.dp),
          )
        }
        Text(
          text = activeAddress?.addressLine ?: "Tap to add your delivery address",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))

    Surface(
      onClick = onSwitchToMart,
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.secondaryContainer,
      border = BorderStroke(1.dp, EmeraldSecondary.copy(alpha = 0.35f)),
      modifier = Modifier.minimumInteractiveComponentSize(),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Filled.Bolt,
          contentDescription = null,
          tint = EmeraldSecondary,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "10m Mart",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
private fun EditorialFoodHeroBanner(onExploreFeatured: () -> Unit) {
  Card(
    onClick = onExploreFeatured,
    shape = RoundedCornerShape(22.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .height(166.dp),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      HeroArtworkImage(
        artwork = com.example.data.model.HeroArtwork.FOOD_FEAST,
        contentDescription = "Chef-curated dinner feast banner",
        modifier = Modifier.fillMaxSize(),
      )
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(16.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primary,
        ) {
          Text(
            text = "USE CODE CRAVE20",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Woodfire Pies & Dum Biryani",
          style = MaterialTheme.typography.headlineMedium,
          color = Color.White,
        )
        Text(
          text = "Up to 20% off tonight • Freshly customized to order",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.92f),
        )
      }
    }
  }
}

@Composable
fun RestaurantCard(
  restaurant: Restaurant,
  isFavorite: Boolean,
  onClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    onClick = onClick,
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
    modifier = modifier.testTag("restaurant_card_${restaurant.id}"),
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(152.dp),
      ) {
        HeroArtworkImage(
          artwork = restaurant.artwork,
          contentDescription = "${restaurant.name} signature dish photo",
          modifier = Modifier.fillMaxSize(),
        )

        if (!restaurant.promoTag.isNullOrBlank()) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(12.dp),
          ) {
            Text(
              text = restaurant.promoTag,
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
          }
        }

        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.36f))
            .testTag("fav_button_${restaurant.id}"),
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Save restaurant",
            tint = if (isFavorite) Color(0xFFFF6B7A) else Color.White,
          )
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color.White.copy(alpha = 0.94f),
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(12.dp),
        ) {
          Text(
            text = "${restaurant.etaMinutes} mins • ${if (restaurant.deliveryFee == 0.0) "Free Delivery" else "$${String.format("%.2f", restaurant.deliveryFee)} delivery"}",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF1C1614),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          )
        }
      }

      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
          ) {
            if (restaurant.isPureVeg) {
              VegIndicatorBadge(isVeg = true)
              Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
              text = restaurant.name,
              style = MaterialTheme.typography.titleLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldSecondary,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = String.format("%.1f", restaurant.rating),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
              )
              Spacer(modifier = Modifier.width(2.dp))
              Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = AmberTertiary.copy(alpha = 1f).let { Color.White },
                modifier = Modifier.size(13.dp),
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "${restaurant.cuisines.joinToString(" • ")}  |  $${restaurant.costForTwo} for two",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = restaurant.tagline,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
