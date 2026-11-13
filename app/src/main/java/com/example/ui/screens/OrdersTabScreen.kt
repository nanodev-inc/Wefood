package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.FoodOrder
import com.example.data.model.OrderStatus
import com.example.data.model.SourceType
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.viewmodel.UiState

@Composable
fun OrdersTabScreen(
  ordersState: UiState<List<FoodOrder>>,
  bottomOverlayPadding: androidx.compose.ui.unit.Dp,
  onExploreFood: () -> Unit,
  onAdvanceOrderStage: (FoodOrder) -> Unit,
  onReorder: (FoodOrder) -> Unit,
  onRateOrder: (orderId: String, stars: Int) -> Unit,
) {
  when (ordersState) {
    UiState.Loading -> {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
      }
    }

    is UiState.Error -> {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = ordersState.message,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.error,
        )
      }
    }

    is UiState.Success -> {
      val orders = ordersState.data
      val activeOrders = orders.filter { OrderStatus.fromWire(it.status) != OrderStatus.DELIVERED }
      val pastOrders = orders.filter { OrderStatus.fromWire(it.status) == OrderStatus.DELIVERED }

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
          Text("Your Orders", style = MaterialTheme.typography.headlineLarge)
          Text(
            text = "Live delivery progress and past favorites",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        if (orders.isEmpty()) {
          item {
            Surface(
              shape = RoundedCornerShape(22.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Icon(
                  imageVector = Icons.Filled.Fastfood,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.empty_orders_title), style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = stringResource(R.string.empty_orders_subtitle),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onExploreFood) {
                  Text("Explore Food & Mart")
                }
              }
            }
          }
        }

        if (activeOrders.isNotEmpty()) {
          item {
            Text(
              text = "Live Tracking (${activeOrders.size})",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.primary,
            )
          }
          items(activeOrders, key = { it.id }) { order ->
            LiveOrderTrackerCard(
              order = order,
              onAdvanceStage = { onAdvanceOrderStage(order) },
            )
          }
        }

        if (pastOrders.isNotEmpty()) {
          item {
            Text(
              text = "Past Orders (${pastOrders.size})",
              style = MaterialTheme.typography.titleLarge,
            )
          }
          items(pastOrders, key = { it.id }) { order ->
            PastOrderCard(
              order = order,
              onReorder = { onReorder(order) },
              onRate = { stars -> onRateOrder(order.id, stars) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun LiveOrderTrackerCard(
  order: FoodOrder,
  onAdvanceStage: () -> Unit,
) {
  val status = OrderStatus.fromWire(order.status)
  val progressFraction = when (status) {
    OrderStatus.CONFIRMED -> 0.18f
    OrderStatus.PREPARING -> 0.48f
    OrderStatus.OUT_FOR_DELIVERY -> 0.78f
    OrderStatus.DELIVERED -> 1f
  }
  val isMart = order.orderType == SourceType.MART.wireValue

  val infiniteTransition = rememberInfiniteTransition(label = "courierPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulse",
  )

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("live_order_card_${order.id}"),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isMart) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
          ) {
            Text(
              text = if (isMart) "QUICK MART LIVE" else "FOOD DELIVERY LIVE",
              style = MaterialTheme.typography.labelSmall,
              color = if (isMart) EmeraldSecondary else CrimsonPrimary,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(order.vendorName, style = MaterialTheme.typography.headlineMedium)
        }

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.primary,
        ) {
          Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = "${order.etaMinutes} min",
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "ETA",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.85f),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Custom Compose Canvas Live Delivery Route
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
          .fillMaxWidth()
          .height(92.dp),
      ) {
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
          val start = Offset(0f, size.height * 0.65f)
          val control = Offset(size.width * 0.5f, -size.height * 0.15f)
          val end = Offset(size.width, size.height * 0.65f)

          val fullPath = Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(control.x, control.y, end.x, end.y)
          }

          drawPath(
            path = fullPath,
            color = Color(0xFFD6C7BA),
            style = Stroke(
              width = 6f,
              cap = StrokeCap.Round,
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f),
            ),
          )

          // Active route segment via quadratic Bezier interpolation
          val t = progressFraction
          val courierX = (1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * control.x + t * t * end.x
          val courierY = (1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * control.y + t * t * end.y
          val courierPoint = Offset(courierX, courierY)

          val activeControl = Offset(
            (1 - t) * start.x + t * control.x,
            (1 - t) * start.y + t * control.y,
          )
          val activePath = Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(activeControl.x, activeControl.y, courierPoint.x, courierPoint.y)
          }
          drawPath(
            path = activePath,
            color = if (isMart) Color(0xFF157A4B) else Color(0xFFC81E32),
            style = Stroke(width = 7f, cap = StrokeCap.Round),
          )

          // Origin & destination pins
          drawCircle(color = Color(0xFF1C1614), radius = 11f, center = start)
          drawCircle(color = Color(0xFF157A4B), radius = 12f, center = end)

          // Pulsing courier halo + marker
          val badgeColor = if (isMart) Color(0xFF157A4B) else Color(0xFFC81E32)
          drawCircle(
            color = badgeColor.copy(alpha = 0.25f),
            radius = 22f * pulseScale,
            center = courierPoint,
          )
          drawCircle(color = badgeColor, radius = 13f, center = courierPoint)
          drawCircle(color = Color.White, radius = 5f, center = courierPoint)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 4-step status progress row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        OrderStatus.entries.forEach { step ->
          val reached = step.stepIndex <= status.stepIndex
          val isCurrent = step == status
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
          ) {
            Box(
              modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                  when {
                    isCurrent -> MaterialTheme.colorScheme.primary
                    reached -> EmeraldSecondary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                  }
                ),
              contentAlignment = Alignment.Center,
            ) {
              if (reached) {
                Icon(
                  Icons.Filled.Check,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(15.dp),
                )
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = when (step) {
                OrderStatus.CONFIRMED -> "Confirmed"
                OrderStatus.PREPARING -> "Preparing"
                OrderStatus.OUT_FOR_DELIVERY -> "On the way"
                OrderStatus.DELIVERED -> "Delivered"
              },
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
              color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline)
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            Icons.Filled.DeliveryDining,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "${order.courierName} is assigned",
              style = MaterialTheme.typography.titleSmall,
            )
            Text(
              text = order.itemsSummary,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        TextButton(
          onClick = onAdvanceStage,
          modifier = Modifier.testTag("advance_order_${order.id}"),
        ) {
          Text("Next Step")
        }
      }
    }
  }
}

@Composable
private fun PastOrderCard(
  order: FoodOrder,
  onReorder: () -> Unit,
  onRate: (Int) -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(order.vendorName, style = MaterialTheme.typography.titleMedium)
          Text(
            text = "${order.itemCount} items • $${String.format("%.2f", order.totalAmount)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
          Text(
            text = "DELIVERED",
            style = MaterialTheme.typography.labelSmall,
            color = EmeraldSecondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = order.itemsSummary,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          (1..5).forEach { star ->
            val filled = star <= order.rating.toInt()
            IconButton(
              onClick = { onRate(star) },
              modifier = Modifier.size(36.dp),
            ) {
              Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "Rate $star stars",
                tint = if (filled) AmberTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }

        OutlinedButton(
          onClick = onReorder,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("reorder_${order.id}"),
        ) {
          Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Reorder")
        }
      }
    }
  }
}
