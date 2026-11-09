package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AlAqsaGreenDarkTheme,
    onPrimary = Color(0xFF003920),
    primaryContainer = AlAqsaGreenContainerDark,
    onPrimaryContainer = Color(0xFFA1F5C6),
    secondary = AlAqsaGoldDarkTheme,
    onSecondary = Color(0xFF422C00),
    secondaryContainer = AlAqsaGoldContainerDark,
    onSecondaryContainer = Color(0xFFFFDF9B),
    tertiary = Color(0xFF8FD0C5),
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    outline = OutlineDark,
    onBackground = Color(0xFFE1E5E1),
    onSurface = Color(0xFFE1E5E1),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AlAqsaGreen,
    onPrimary = Color.White,
    primaryContainer = AlAqsaGreenContainer,
    onPrimaryContainer = OnAlAqsaGreenContainer,
    secondary = AlAqsaGold,
    onSecondary = Color.White,
    secondaryContainer = AlAqsaGoldContainer,
    onSecondaryContainer = OnAlAqsaGoldContainer,
    tertiary = Color(0xFF006A60),
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    outline = OutlineLight,
    onBackground = Color(0xFF171D19),
    onSurface = Color(0xFF171D19),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set default dynamicColor to false to maintain the mandated Al-Aqsa green & gold identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
