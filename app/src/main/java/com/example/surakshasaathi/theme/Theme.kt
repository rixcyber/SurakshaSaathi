package com.example.surakshasaathi.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = SurakshaPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = SurakshaPrimary,
    onPrimaryContainer = Color.White,
    secondary = MessageShieldColor,
    onSecondary = Color.White,
    tertiary = LinkShieldColor,
    onTertiary = Color.White,
    background = SurfaceDark,
    onBackground = TextPrimaryDark,
    surface = CardDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    error = StopRed,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = SurakshaPrimary,
    onPrimary = Color.White,
    primaryContainer = SurakshaPrimaryLight,
    onPrimaryContainer = Color.White,
    secondary = MessageShieldColor,
    onSecondary = Color.White,
    tertiary = LinkShieldColor,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = CardLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    error = StopRed,
    onError = Color.White,
)

@Composable
fun SurakshaSaathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to keep our brand colors consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
