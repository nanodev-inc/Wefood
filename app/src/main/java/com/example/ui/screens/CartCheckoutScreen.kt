package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CartEntry
import com.example.data.model.SavedAddress
import com.example.data.model.SourceType
import com.example.data.repository.CatalogRepository
import com.example.ui.components.VegIndicatorBadge
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.viewmodel.CartPricingSummary

@Composable
fun CartCheckoutScreen(
  cartItems: List<CartEntry>,
  pricing: CartPricingSummary,
  activeAddress: SavedAddress?,
  isPlacingOrder: Boolean,
  onBack: () -> Unit,
  onUpdateQuantity: (cartItemId: String, newQty: Int) -> Unit,
  onClearCart: () -> Unit,
  onApplyCoupon: (String) -> Unit,
  onRemoveCoupon: () -> Unit,
  onChangeAddress: () -> Unit,
  onPlaceOrder: () -> Unit,
) {
  BackHandler(onBack = onBack)

  var couponInput by remember { mutableStateOf("") }

  Column(modifier = Modifier.fillMaxSize()) {
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("cart_back_button"),
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
          Column {
            Text("Checkout", style = MaterialTheme.typography.headlineMedium)
            if (cartItems.isNotEmpty()) {
              val first = cartItems.first()
              Text(
                text = "${first.vendorName} • ${if (first.sourceType == SourceType.MART.wireValue) "10-Min Quick Mart" else "Restaurant Delivery"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
        if (cartItems.isNotEmpty()) {
          TextButton(onClick = onClearCart) {
            Text("Clear", color = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }

    if (cartItems.isEmpty()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Icon(
          imageVector = Icons.Filled.ShoppingBag,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(56.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(stringResource(R.string.empty_cart_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = stringResource(R.string.empty_cart_subtitle),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onBack) {
          Text("Browse Restaurants & Mart")
        }
      }
      return
    }

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Filled.LocationOn,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Delivering to ${activeAddress?.label ?: "Address needed"}",
                style = MaterialTheme.typography.titleSmall,
              )
              Text(
                text = activeAddress?.addressLine ?: "Please add a delivery address",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            TextButton(onClick = onChangeAddress) {
              Text("Change")
            }
          }
        }
      }

      items(cartItems, key = { it.id }) { entry ->
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            VegIndicatorBadge(isVeg = entry.isVeg)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(entry.itemName, style = MaterialTheme.typography.titleSmall)
              if (entry.optionsSummary.isNotBlank()) {
                Text(
                  text = entry.optionsSummary,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              if (entry.specialNote.isNotBlank()) {
                Text(
                  text = "Note: ${entry.specialNote}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "$${String.format("%.2f", entry.unitPrice * entry.quantity)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
              )
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = { onUpdateQuantity(entry.id, (entry.quantity - 1).toInt()) },
                  modifier = Modifier.size(36.dp),
                ) {
                  Icon(
                    Icons.Filled.Remove,
                    contentDescription = "Decrease ${entry.itemName}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                  )
                }
                Text(
                  text = entry.quantity.toString(),
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 4.dp),
                )
                IconButton(
                  onClick = { onUpdateQuantity(entry.id, (entry.quantity + 1).toInt()) },
                  modifier = Modifier.size(36.dp),
                ) {
                  Icon(
                    Icons.Filled.Add,
                    contentDescription = "Increase ${entry.itemName}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                  )
                }
              }
            }
          }
        }
      }

      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.LocalOffer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Apply Promo Coupon", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (pricing.appliedCoupon != null) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      Icons.Filled.CheckCircle,
                      contentDescription = null,
                      tint = EmeraldSecondary,
                      modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "${pricing.appliedCoupon.code} applied • ${pricing.appliedCoupon.title}",
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                  }
                  TextButton(onClick = onRemoveCoupon) {
                    Text("Remove")
                  }
                }
              }
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                OutlinedTextField(
                  value = couponInput,
                  onValueChange = { couponInput = it.uppercase() },
                  placeholder = { Text("Enter CRAVE20 or FREEDEL") },
                  singleLine = true,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("coupon_input"),
                )
                OutlinedButton(
                  onClick = { onApplyCoupon(couponInput) },
                  enabled = couponInput.isNotBlank(),
                  modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("apply_coupon_button"),
                ) {
                  Text("Apply")
                }
              }
              Spacer(modifier = Modifier.height(10.dp))
              LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CatalogRepository.coupons) { coupon ->
                  Surface(
                    onClick = { onApplyCoupon(coupon.code) },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                  ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                      Text(
                        text = coupon.code,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                      )
                      Text(
                        text = coupon.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text("Bill Summary", style = MaterialTheme.typography.titleMedium)
            BillRow("Item Total", "$${String.format("%.2f", pricing.subtotal)}")
            BillRow(
              "Delivery Partner Fee",
              if (pricing.deliveryFee == 0.0) "FREE" else "$${String.format("%.2f", pricing.deliveryFee)}",
              highlight = pricing.deliveryFee == 0.0,
            )
            if (pricing.discount > 0.0) {
              BillRow(
                "Promo Savings",
                "-$${String.format("%.2f", pricing.discount)}",
                highlight = true,
              )
            }
            BillRow("Taxes & Packaging (5%)", "$${String.format("%.2f", pricing.taxesAndCharges)}")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("To Pay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text(
                text = "$${String.format("%.2f", pricing.grandTotal)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }

    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 10.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Button(
        onClick = onPlaceOrder,
        enabled = !isPlacingOrder && activeAddress != null,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
          .height(54.dp)
          .minimumInteractiveComponentSize()
          .testTag("place_order_button"),
      ) {
        if (isPlacingOrder) {
          CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.dp,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text("Placing Order…", style = MaterialTheme.typography.titleSmall)
        } else {
          Text(
            text = "${stringResource(R.string.place_order)} • $${String.format("%.2f", pricing.grandTotal)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

@Composable
private fun BillRow(label: String, value: String, highlight: Boolean = false) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = if (highlight) EmeraldSecondary else MaterialTheme.colorScheme.onSurface,
      fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
    )
  }
}
