package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MenuItem
import com.example.data.model.Restaurant
import com.example.data.repository.CatalogRepository
import com.example.ui.components.HeroArtworkImage
import com.example.ui.components.VegIndicatorBadge
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.EmeraldSecondary

@Composable
fun RestaurantDetailScreen(
  restaurant: Restaurant,
  isFavorite: Boolean,
  bottomOverlayPadding: androidx.compose.ui.unit.Dp,
  onBack: () -> Unit,
  onToggleFavorite: () -> Unit,
  onAddCustomizedDish: (
    dish: MenuItem,
    unitPrice: Double,
    quantity: Int,
    optionsSummary: String,
    specialNote: String,
  ) -> Unit,
) {
  BackHandler(onBack = onBack)

  val allMenuItems = remember(restaurant.id) { CatalogRepository.getMenuForRestaurant(restaurant.id) }
  var vegOnly by remember { mutableStateOf(false) }
  var bestsellerOnly by remember { mutableStateOf(false) }
  var dishToCustomize by remember { mutableStateOf<MenuItem?>(null) }

  val filteredMenu = remember(allMenuItems, vegOnly, bestsellerOnly) {
    allMenuItems.filter { item ->
      (!vegOnly || item.isVeg) && (!bestsellerOnly || item.isBestseller)
    }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = bottomOverlayPadding + 24.dp),
  ) {
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(210.dp),
      ) {
        HeroArtworkImage(
          artwork = restaurant.artwork,
          contentDescription = restaurant.name,
          modifier = Modifier.fillMaxSize(),
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.48f))
              .testTag("restaurant_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to restaurants",
              tint = Color.White,
            )
          }
          IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.48f)),
          ) {
            Icon(
              imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
              contentDescription = "Toggle favorite",
              tint = if (isFavorite) Color(0xFFFF6B7A) else Color.White,
            )
          }
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = restaurant.name,
              style = MaterialTheme.typography.headlineLarge,
              modifier = Modifier.weight(1f),
            )
            Surface(shape = RoundedCornerShape(10.dp), color = EmeraldSecondary) {
              Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = String.format("%.1f", restaurant.rating),
                  style = MaterialTheme.typography.labelLarge,
                  color = Color.White,
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
              }
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${restaurant.cuisines.joinToString(", ")} • ${restaurant.area}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
              Text(
                text = "${restaurant.etaMinutes} mins delivery",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              )
            }
            if (!restaurant.promoTag.isNullOrBlank()) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
              ) {
                Text(
                  text = restaurant.promoTag,
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
            }
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        FilterChip(
          selected = vegOnly,
          onClick = { vegOnly = !vegOnly },
          label = { Text("Veg Only") },
          leadingIcon = { VegIndicatorBadge(isVeg = true) },
        )
        FilterChip(
          selected = bestsellerOnly,
          onClick = { bestsellerOnly = !bestsellerOnly },
          label = { Text("Bestsellers") },
          leadingIcon = {
            Icon(Icons.Filled.Star, contentDescription = null, tint = AmberTertiary, modifier = Modifier.size(16.dp))
          },
        )
      }
    }

    item {
      Text(
        text = "Menu (${filteredMenu.size})",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      )
    }

    items(filteredMenu, key = { it.id }) { dish ->
      DishMenuCard(
        dish = dish,
        onAddOrCustomize = {
          if (dish.customizationGroups.isNotEmpty()) {
            dishToCustomize = dish
          } else {
            onAddCustomizedDish(dish, dish.basePrice, 1, "", "")
          }
        },
        onCustomizeClick = { dishToCustomize = dish },
      )
    }
  }

  dishToCustomize?.let { dish ->
    DishCustomizerSheet(
      dish = dish,
      onDismiss = { dishToCustomize = null },
      onConfirmAdd = { unitPrice, qty, summary, note ->
        onAddCustomizedDish(dish, unitPrice, qty, summary, note)
        dishToCustomize = null
      },
    )
  }
}

@Composable
private fun DishMenuCard(
  dish: MenuItem,
  onAddOrCustomize: () -> Unit,
  onCustomizeClick: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clickable(onClick = onCustomizeClick),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          VegIndicatorBadge(isVeg = dish.isVeg)
          if (dish.isBestseller) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
              Text(
                text = "BESTSELLER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(dish.name, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "$${String.format("%.2f", dish.basePrice)}",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = dish.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (dish.customizationGroups.isNotEmpty()) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Tune,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Customizable (${dish.customizationGroups.size} options)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      OutlinedButton(
        onClick = onAddOrCustomize,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.outlinedButtonColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
          contentColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = Modifier
          .minimumInteractiveComponentSize()
          .testTag("add_dish_${dish.id}"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (dish.customizationGroups.isNotEmpty()) "Customize" else "ADD",
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishCustomizerSheet(
  dish: MenuItem,
  onDismiss: () -> Unit,
  onConfirmAdd: (unitPrice: Double, quantity: Int, optionsSummary: String, specialNote: String) -> Unit,
) {
  val selectedRadio = remember(dish.id) {
    mutableStateMapOf<String, String>().apply {
      dish.customizationGroups.filter { it.maxSelection == 1 }.forEach { group ->
        group.options.firstOrNull()?.let { put(group.id, it.id) }
      }
    }
  }
  val selectedMulti = remember(dish.id) {
    mutableStateMapOf<String, Set<String>>()
  }
  var quantity by remember(dish.id) { mutableIntStateOf(1) }
  var specialNote by remember(dish.id) { mutableStateOf("") }

  val chosenOptions = dish.customizationGroups.flatMap { group ->
    if (group.maxSelection == 1) {
      val id = selectedRadio[group.id]
      group.options.filter { it.id == id }
    } else {
      val ids = selectedMulti[group.id].orEmpty()
      group.options.filter { it.id in ids }
    }
  }
  val unitPrice = dish.basePrice + chosenOptions.sumOf { it.extraPrice }
  val totalPrice = unitPrice * quantity

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 24.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        VegIndicatorBadge(isVeg = dish.isVeg)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = dish.name,
          style = MaterialTheme.typography.headlineMedium,
        )
      }
      Text(
        text = dish.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.outline)

      Column(
        modifier = Modifier
          .weight(1f, fill = false)
          .verticalScroll(rememberScrollState())
          .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        dish.customizationGroups.forEach { group ->
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(group.title, style = MaterialTheme.typography.titleMedium)
              Text(
                text = if (group.isRequired) "Required" else "Up to ${group.maxSelection}",
                style = MaterialTheme.typography.labelSmall,
                color = if (group.isRequired) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Spacer(modifier = Modifier.height(6.dp))

            group.options.forEach { option ->
              val isSingle = group.maxSelection == 1
              val isSelected = if (isSingle) {
                selectedRadio[group.id] == option.id
              } else {
                selectedMulti[group.id].orEmpty().contains(option.id)
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    if (isSingle) {
                      selectedRadio[group.id] = option.id
                    } else {
                      val current = selectedMulti[group.id].orEmpty().toMutableSet()
                      if (current.contains(option.id)) {
                        current.remove(option.id)
                      } else if (current.size < group.maxSelection) {
                        current.add(option.id)
                      }
                      selectedMulti[group.id] = current
                    }
                  }
                  .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f),
                ) {
                  if (isSingle) {
                    RadioButton(
                      selected = isSelected,
                      onClick = { selectedRadio[group.id] = option.id },
                    )
                  } else {
                    Checkbox(
                      checked = isSelected,
                      onCheckedChange = { checked ->
                        val current = selectedMulti[group.id].orEmpty().toMutableSet()
                        if (checked && current.size < group.maxSelection) {
                          current.add(option.id)
                        } else if (!checked) {
                          current.remove(option.id)
                        }
                        selectedMulti[group.id] = current
                      },
                    )
                  }
                  Text(option.name, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                  text = if (option.extraPrice > 0) "+$${String.format("%.2f", option.extraPrice)}" else "Included",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = specialNote,
          onValueChange = { if (it.length <= 140) specialNote = it },
          label = { Text("Kitchen instructions (e.g., extra crispy, sauce on side)") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dish_special_note_input"),
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline)
      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { if (quantity > 1) quantity-- },
              enabled = quantity > 1,
            ) {
              Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity")
            }
            Text(
              text = quantity.toString(),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp),
            )
            IconButton(
              onClick = { if (quantity < 20) quantity++ },
            ) {
              Icon(Icons.Filled.Add, contentDescription = "Increase quantity")
            }
          }
        }

        Button(
          onClick = {
            val summary = chosenOptions.joinToString(" • ") { it.name }
            onConfirmAdd(unitPrice, quantity, summary, specialNote)
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .minimumInteractiveComponentSize()
            .testTag("confirm_add_dish_button"),
        ) {
          Text(
            text = "Add Item • $${String.format("%.2f", totalPrice)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}
