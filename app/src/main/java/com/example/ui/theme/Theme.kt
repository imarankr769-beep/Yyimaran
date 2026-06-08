package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = DarkPrimary,
  onPrimary = DarkOnPrimary,
  primaryContainer = DarkPrimaryContainer,
  onPrimaryContainer = DarkOnPrimaryContainer,
  secondary = DarkSecondary,
  onSecondary = DarkOnSecondary,
  background = DarkBackground,
  onBackground = DarkOnBackground,
  surface = DarkSurface,
  onSurface = DarkOnSurface,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark theme by default for ImaranFlix
  dynamicColor: Boolean = false, // Disable dynamic colors to keep original Netflix cinematic look
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
