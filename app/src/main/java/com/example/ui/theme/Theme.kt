package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
  primary = OrangePrimary,
  onPrimary = Color.White,
  primaryContainer = ApricotContainer,
  onPrimaryContainer = ApricotDarkContainer,
  secondary = EmeraldSecondary,
  onSecondary = Color.White,
  secondaryContainer = MintContainer,
  onSecondaryContainer = MintDarkContainer,
  tertiary = AmberTertiary,
  onTertiary = Color.White,
  tertiaryContainer = AmberContainer,
  onTertiaryContainer = AmberDarkContainer,
  background = CrispWhiteBackground,
  onBackground = DeepCharcoalText,
  surface = PureWhiteSurface,
  onSurface = DeepCharcoalText,
  surfaceVariant = SoftPeachSurfaceVariant,
  onSurfaceVariant = MutedSlateText,
  outline = CrispBorderOutline,
  outlineVariant = CrispBorderOutline,
  error = Color(0xFFDC2626),
  onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
  primary = OrangeLight,
  onPrimary = Color(0xFF421700),
  primaryContainer = ApricotDarkContainer,
  onPrimaryContainer = ApricotContainer,
  secondary = EmeraldLight,
  onSecondary = Color(0xFF003919),
  secondaryContainer = MintDarkContainer,
  onSecondaryContainer = MintContainer,
  tertiary = AmberLight,
  onTertiary = Color(0xFF3E2000),
  tertiaryContainer = AmberDarkContainer,
  onTertiaryContainer = AmberContainer,
  background = MidnightBackground,
  onBackground = CrispOnDarkText,
  surface = MidnightSurface,
  onSurface = CrispOnDarkText,
  surfaceVariant = MidnightSurfaceVariant,
  onSurfaceVariant = MutedOnDarkText,
  outline = MidnightOutline,
  outlineVariant = MidnightOutline,
  error = Color(0xFFF87171),
  onError = Color(0xFF5C0A0A),
)

val CraveCartShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
  extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun CraveCartTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = CraveCartShapes,
    content = content,
  )
}
