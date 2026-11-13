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
  primary = CrimsonPrimary,
  onPrimary = Color.White,
  primaryContainer = BlushContainer,
  onPrimaryContainer = BlushDarkContainer,
  secondary = EmeraldSecondary,
  onSecondary = Color.White,
  secondaryContainer = MintContainer,
  onSecondaryContainer = MintDarkContainer,
  tertiary = AmberTertiary,
  onTertiary = Color.White,
  tertiaryContainer = AmberContainer,
  onTertiaryContainer = AmberDarkContainer,
  background = WarmCreamBackground,
  onBackground = WarmCharcoalText,
  surface = PureWhiteSurface,
  onSurface = WarmCharcoalText,
  surfaceVariant = WarmLinenSurfaceVariant,
  onSurfaceVariant = WarmSlateMutedText,
  outline = WarmSandOutline,
  outlineVariant = WarmSandOutline,
  error = Color(0xFFB3261E),
  onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
  primary = CrimsonLight,
  onPrimary = Color(0xFF47000A),
  primaryContainer = BlushDarkContainer,
  onPrimaryContainer = BlushContainer,
  secondary = EmeraldLight,
  onSecondary = Color(0xFF00391D),
  secondaryContainer = MintDarkContainer,
  onSecondaryContainer = MintContainer,
  tertiary = AmberLight,
  onTertiary = Color(0xFF3E2000),
  tertiaryContainer = AmberDarkContainer,
  onTertiaryContainer = AmberContainer,
  background = EspressoBackground,
  onBackground = CreamOnDarkText,
  surface = EspressoSurface,
  onSurface = CreamOnDarkText,
  surfaceVariant = EspressoSurfaceVariant,
  onSurfaceVariant = WarmStoneOnDarkMuted,
  outline = EspressoOutline,
  outlineVariant = EspressoOutline,
  error = Color(0xFFF2B8B5),
  onError = Color(0xFF601410),
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
