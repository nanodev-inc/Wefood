package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CartEntry
import com.example.data.model.GroceryCategory
import com.example.data.model.GroceryItem
import com.example.data.model.HeroArtwork
import com.example.data.model.SavedAddress
import com.example.data.model.SourceType
import com.example.data.repository.CatalogRepository
import com.example.ui.components.HeroArtworkImage
import com.example.ui.components.VegIndicatorBadge
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.viewmodel.MartFilterState

@Composable
fun MartTabScreen(
  activeAddress: SavedAddress?,
  filterState: MartFilterState,
  groceryItems: List<GroceryItem>,
  cartItems: List<CartEntry>,
  bottomOverlayPadding: androidx.compose.ui.unit.Dp,
  onOpenAddressPicker: () -> Unit,
  onSearchChange: (String) -> Unit,
  onSelectCategory: (String?) -> Unit,
  onIncrementItem: (GroceryItem) -> Unit,
  onDecrementItem: (GroceryItem) -> Unit,
) {
  val martCartByItemId = cartItems
    .filter { it.sourceType == SourceType.MART.wireValue }
    .associateBy { it.itemId }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val columns = if (maxWidth >= 680.dp) 3 else 2

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = bottomOverlayPadding + 20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Surface(
          color = EmeraldSecondary,
          contentColor = Color.White,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Filled.Bolt,
                  contentDescription = null,
                  tint = Color(0xFFFDE047),
                  modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "CRAVECART QUICK MART",
                  style = MaterialTheme.typography.labelLarge,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                )
              }
              Surface(
                onClick = onOpenAddressPicker,
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.16f),
              ) {
                Text(
                  text = activeAddress?.label ?: "Set Address",
                  style = MaterialTheme.typography.labelMedium,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Fresh groceries in 10 minutes",
              style = MaterialTheme.typography.headlineLarge,
              color = Color.White,
            )
            Text(
              text = "Free delivery on baskets $15+ • Use code MART15",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.9f),
            )
          }
        }
      }

      item {
        OutlinedTextField(
          value = filterState.searchQuery,
          onValueChange = onSearchChange,
          placeholder = { Text(stringResource(R.string.search_mart_hint)) },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search groceries") },
          trailingIcon = {
            if (filterState.searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchChange("") }) {
                Icon(Icons.Filled.Close, contentDescription = "Clear grocery search")
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
            .testTag("mart_search_input"),
        )
      }

      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(140.dp),
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            HeroArtworkImage(
              artwork = HeroArtwork.MART_BASKET,
              contentDescription = "Fresh farm produce and bakery grocery basket",
              modifier = Modifier.fillMaxSize(),
            )
            Column(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            ) {
              Surface(shape = RoundedCornerShape(8.dp), color = EmeraldSecondary) {
                Text(
                  text = "MORNING HARVEST DROP",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Farm Fruits, Sourdough & Dairy",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
              )
            }
          }
        }
      }

      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Shop by Aisle", style = MaterialTheme.typography.headlineMedium)
            if (filterState.selectedCategoryId != null) {
              OutlinedButton(onClick = { onSelectCategory(null) }) {
                Text("All Aisles")
              }
            }
          }
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            items(CatalogRepository.groceryCategories, key = { it.id }) { cat ->
              val selected = filterState.selectedCategoryId == cat.id
              MartCategoryTile(
                category = cat,
                selected = selected,
                onClick = { onSelectCategory(cat.id) },
              )
            }
          }
        }
      }

      item {
        val activeCategoryName = CatalogRepository.groceryCategories
          .firstOrNull { it.id == filterState.selectedCategoryId }?.name ?: "All Quick Mart Essentials"
        Text(
          text = "$activeCategoryName (${groceryItems.size})",
          style = MaterialTheme.typography.headlineMedium,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }

      val rows = groceryItems.chunked(columns)
      items(rows) { rowItems ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          rowItems.forEach { item ->
            val qty = martCartByItemId[item.id]?.quantity?.toInt() ?: 0
            GroceryItemCard(
              item = item,
              quantityInCart = qty,
              onAdd = { onIncrementItem(item) },
              onDecrement = { onDecrementItem(item) },
              modifier = Modifier.weight(1f),
            )
          }
          repeat(columns - rowItems.size) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}

@Composable
private fun MartCategoryTile(
  category: GroceryCategory,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val icon = when (category.id) {
    "cat_produce" -> Icons.Filled.Eco
    "cat_dairy" -> Icons.Filled.BakeryDining
    "cat_snacks" -> Icons.Filled.LocalCafe
    "cat_pantry" -> Icons.Filled.Kitchen
    "cat_bakery" -> Icons.Filled.ShoppingBasket
    else -> Icons.Filled.Icecream
  }
  val accent = Color(category.accentHex)

  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    color = if (selected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
    border = BorderStroke(
      width = if (selected) 2.dp else 1.dp,
      color = if (selected) accent else MaterialTheme.colorScheme.outline,
    ),
    modifier = Modifier
      .width(136.dp)
      .minimumInteractiveComponentSize(),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
        }
        Text(
          text = category.badge,
          style = MaterialTheme.typography.labelSmall,
          color = accent,
          fontWeight = FontWeight.Bold,
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = category.name,
        style = MaterialTheme.typography.titleSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = category.subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun GroceryItemCard(
  item: GroceryItem,
  quantityInCart: Int,
  onAdd: () -> Unit,
  onDecrement: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        VegIndicatorBadge(isVeg = item.isVeg)
        if (!item.discountBadge.isNullOrBlank()) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
          ) {
            Text(
              text = item.discountBadge,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = item.name,
        style = MaterialTheme.typography.titleSmall,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        minLines = 2,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = item.unitLabel,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "$${String.format("%.2f", item.price)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          if (item.mrp > item.price) {
            Text(
              text = "$${String.format("%.2f", item.mrp)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textDecoration = TextDecoration.LineThrough,
            )
          }
        }

        if (quantityInCart <= 0) {
          OutlinedButton(
            onClick = onAdd,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.5.dp, EmeraldSecondary),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
              contentColor = EmeraldSecondary,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("mart_add_${item.id}"),
          ) {
            Text("ADD", fontWeight = FontWeight.Bold)
          }
        } else {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = EmeraldSecondary,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(36.dp),
              ) {
                Icon(
                  Icons.Filled.Remove,
                  contentDescription = "Remove one ${item.name}",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp),
                )
              }
              Text(
                text = quantityInCart.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
              )
              IconButton(
                onClick = onAdd,
                modifier = Modifier.size(36.dp),
              ) {
                Icon(
                  Icons.Filled.Add,
                  contentDescription = "Add one more ${item.name}",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}
