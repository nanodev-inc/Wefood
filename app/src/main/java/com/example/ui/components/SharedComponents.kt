package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.FoodOrder
import com.example.data.model.HeroArtwork
import com.example.data.model.OrderStatus
import com.example.data.model.SavedAddress
import com.example.ui.theme.EmeraldSecondary

@Composable
fun VegIndicatorBadge(isVeg: Boolean, modifier: Modifier = Modifier) {
  val color = if (isVeg) EmeraldSecondary else MaterialTheme.colorScheme.primary
  val label = if (isVeg) "Vegetarian" else "Non-vegetarian"
  Box(
    modifier = modifier
      .size(16.dp)
      .border(1.5.dp, color, RoundedCornerShape(4.dp))
      .semantics { contentDescription = label },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .size(7.dp)
        .clip(CircleShape)
        .background(color)
    )
  }
}

@Composable
fun HeroArtworkImage(
  artwork: HeroArtwork,
  contentDescription: String,
  modifier: Modifier = Modifier,
) {
  val drawableId = when (artwork) {
    HeroArtwork.FOOD_FEAST -> R.drawable.img_hero_food
    HeroArtwork.FEATURED_BOWL -> R.drawable.img_dish_featured
    HeroArtwork.MART_BASKET -> R.drawable.img_hero_mart
  }
  Box(modifier = modifier) {
    Image(
      painter = painterResource(id = drawableId),
      contentDescription = contentDescription,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f))
          )
        )
    )
  }
}

@Composable
fun FloatingCartBar(
  visible: Boolean,
  itemCount: Int,
  vendorName: String,
  subtotal: Double,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = visible && itemCount > 0,
    enter = slideInVertically(
      initialOffsetY = { it / 2 },
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
    modifier = modifier,
  ) {
    Surface(
      onClick = onClick,
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      shadowElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .minimumInteractiveComponentSize()
        .testTag("floating_cart_bar"),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f),
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.ShoppingBag,
              contentDescription = "Shopping cart",
              tint = Color.White,
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "$itemCount ${if (itemCount == 1) "item" else "items"} • $${String.format("%.2f", subtotal)}",
              style = MaterialTheme.typography.titleSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = vendorName,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.88f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "View Cart",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Open cart",
            tint = Color.White,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@Composable
fun LiveOrderMiniBanner(
  order: FoodOrder,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val status = OrderStatus.fromWire(order.status)
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .minimumInteractiveComponentSize()
      .testTag("live_order_banner"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        Icon(
          imageVector = Icons.Filled.DeliveryDining,
          contentDescription = "Live delivery status",
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "${status.label} • ${order.etaMinutes} min ETA",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "${order.vendorName} • Tap to track live",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      Text(
        text = "TRACK",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressPickerBottomSheet(
  addresses: List<SavedAddress>,
  activeAddressId: String?,
  onSelectAddress: (String) -> Unit,
  onAddNewAddress: (label: String, addressLine: String, landmark: String) -> Unit,
  onDeleteAddress: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var showAddForm by remember { mutableStateOf(false) }
  var newLabel by remember { mutableStateOf("Home") }
  var newLine by remember { mutableStateOf("") }
  var newLandmark by remember { mutableStateOf("") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 28.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (showAddForm) "Add Delivery Address" else "Deliver To",
          style = MaterialTheme.typography.headlineMedium,
        )
        if (!showAddForm) {
          TextButton(
            onClick = { showAddForm = true },
            modifier = Modifier.testTag("add_address_toggle"),
          ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add New")
          }
        }
      }
      Spacer(modifier = Modifier.height(12.dp))

      if (showAddForm) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("Home", "Work", "Other").forEach { option ->
            FilterChip(
              selected = newLabel == option,
              onClick = { newLabel = option },
              label = { Text(option) },
            )
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = newLine,
          onValueChange = { newLine = it },
          label = { Text("Street, Apt / House No.") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("address_line_input"),
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = newLandmark,
          onValueChange = { newLandmark = it },
          label = { Text("Landmark (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
            onClick = { showAddForm = false },
            modifier = Modifier
              .weight(1f)
              .minimumInteractiveComponentSize(),
          ) {
            Text("Back")
          }
          Button(
            onClick = {
              onAddNewAddress(newLabel, newLine, newLandmark)
              showAddForm = false
              newLine = ""
              newLandmark = ""
              onDismiss()
            },
            enabled = newLine.trim().length >= 5,
            modifier = Modifier
              .weight(1f)
              .minimumInteractiveComponentSize()
              .testTag("save_address_button"),
          ) {
            Text("Save Address")
          }
        }
      } else {
        if (addresses.isEmpty()) {
          Text(
            text = "No saved addresses yet. Add one so riders can reach you fast.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        } else {
          addresses.forEach { addr ->
            val isSelected = addr.id == activeAddressId
            val icon = when (addr.label) {
              "Home" -> Icons.Filled.Home
              "Work" -> Icons.Filled.Work
              else -> Icons.Filled.LocationOn
            }
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .clickable {
                  onSelectAddress(addr.id)
                  onDismiss()
                },
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                RadioButton(
                  selected = isSelected,
                  onClick = {
                    onSelectAddress(addr.id)
                    onDismiss()
                  },
                )
                Icon(
                  imageVector = icon,
                  contentDescription = addr.label,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(addr.label, style = MaterialTheme.typography.titleSmall)
                    if (isSelected) {
                      Spacer(modifier = Modifier.width(6.dp))
                      Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Active address",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp),
                      )
                    }
                  }
                  Text(
                    text = addr.addressLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  if (addr.landmark.isNotBlank()) {
                    Text(
                      text = addr.landmark,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
                if (addresses.size > 1) {
                  IconButton(onClick = { onDeleteAddress(addr.id) }) {
                    Icon(
                      imageVector = Icons.Filled.DeleteOutline,
                      contentDescription = "Delete ${addr.label} address",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun CartVendorConflictDialog(
  currentVendor: String,
  newVendor: String,
  onReplace: () -> Unit,
  onCancel: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onCancel,
    title = {
      Text("Replace cart items?", style = MaterialTheme.typography.headlineMedium)
    },
    text = {
      Text(
        "Your cart currently has items from $currentVendor. Would you like to clear it and start a fresh order from $newVendor?",
        style = MaterialTheme.typography.bodyMedium,
      )
    },
    confirmButton = {
      Button(
        onClick = onReplace,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
      ) {
        Text("Replace Cart")
      }
    },
    dismissButton = {
      TextButton(onClick = onCancel) {
        Text("Keep Existing")
      }
    },
  )
}
