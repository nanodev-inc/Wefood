package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Restaurant
import com.example.data.model.SavedAddress
import com.example.data.preferences.AppSettings
import com.example.data.preferences.ThemeMode
import com.example.data.repository.CatalogRepository
import com.example.ui.components.VegIndicatorBadge

@Composable
fun SavedTabScreen(
  displayName: String,
  email: String,
  appSettings: AppSettings,
  addresses: List<SavedAddress>,
  activeAddressId: String?,
  favoriteRestaurants: List<Restaurant>,
  bottomOverlayPadding: androidx.compose.ui.unit.Dp,
  onThemeModeChange: (ThemeMode) -> Unit,
  onDefaultVegOnlyChange: (Boolean) -> Unit,
  onOrderAlertsChange: (Boolean) -> Unit,
  onOpenAddressPicker: () -> Unit,
  onSelectAddress: (String) -> Unit,
  onOpenRestaurant: (Restaurant) -> Unit,
  onSignOut: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(
      start = 16.dp,
      end = 16.dp,
      top = 12.dp,
      bottom = bottomOverlayPadding + 24.dp,
    ),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Text("Profile & Settings", style = MaterialTheme.typography.headlineLarge)
    }

    item {
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "User avatar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(displayName, style = MaterialTheme.typography.titleLarge)
              if (email.isNotBlank()) {
                Text(
                  text = email,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
          OutlinedButton(
            onClick = onSignOut,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("sign_out_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Logout,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sign Out")
          }
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("App Preferences", style = MaterialTheme.typography.titleMedium)

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.DarkMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Appearance Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              ThemeMode.entries.forEach { mode ->
                FilterChip(
                  selected = appSettings.themeMode == mode,
                  onClick = { onThemeModeChange(mode) },
                  label = { Text(mode.label) },
                  modifier = Modifier.testTag("theme_chip_${mode.name.lowercase()}"),
                )
              }
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              VegIndicatorBadge(isVeg = true)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Default Pure Veg Filter", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                  "Start restaurant browsing with vegetarian kitchens first",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            Switch(
              checked = appSettings.defaultVegOnly,
              onCheckedChange = onDefaultVegOnlyChange,
              modifier = Modifier.testTag("veg_default_switch"),
            )
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Live Order Status Alerts", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                  "Show live delivery banner while orders are active",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            Switch(
              checked = appSettings.orderAlertsEnabled,
              onCheckedChange = onOrderAlertsChange,
              modifier = Modifier.testTag("order_alerts_switch"),
            )
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Saved Addresses", style = MaterialTheme.typography.headlineMedium)
        TextButton(onClick = onOpenAddressPicker) {
          Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Manage")
        }
      }
    }

    items(addresses, key = { it.id }) { addr ->
      val isActive = addr.id == activeAddressId
      val icon = when (addr.label) {
        "Home" -> Icons.Filled.Home
        "Work" -> Icons.Filled.Work
        else -> Icons.Filled.LocationOn
      }
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
          1.dp,
          if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSelectAddress(addr.id) },
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(icon, contentDescription = addr.label, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(addr.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
              text = addr.addressLine,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          if (isActive) {
            Text(
              text = "ACTIVE",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }

    item {
      Text("Favorite Kitchens (${favoriteRestaurants.size})", style = MaterialTheme.typography.headlineMedium)
    }

    if (favoriteRestaurants.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "Tap the heart icon on any restaurant card to save your go-to spots here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
          )
        }
      }
    } else {
      items(favoriteRestaurants, key = { it.id }) { rest ->
        Surface(
          onClick = { onOpenRestaurant(rest) },
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(rest.name, style = MaterialTheme.typography.titleMedium)
              Text(
                text = "${rest.cuisines.joinToString(", ")} • ${rest.etaMinutes} mins",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Text("Menu", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }

    item {
      Text("Available Promo Codes", style = MaterialTheme.typography.headlineMedium)
    }

    items(CatalogRepository.coupons, key = { it.code }) { coupon ->
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "${coupon.code} — ${coupon.title}",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = coupon.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}
