package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val SanadDarkColorScheme = darkColorScheme(
    primary = DarkSanadColors.accent,
    onPrimary = DarkSanadColors.onAccent,
    primaryContainer = DarkSanadColors.surface,
    onPrimaryContainer = DarkSanadColors.text,
    secondary = DarkSanadColors.accent,
    onSecondary = DarkSanadColors.onAccent,
    secondaryContainer = DarkSanadColors.divider,
    onSecondaryContainer = DarkSanadColors.text,
    tertiary = DarkSanadColors.busy,
    onTertiary = DarkSanadColors.onAccent,
    tertiaryContainer = DarkSanadColors.surface,
    onTertiaryContainer = DarkSanadColors.text,
    background = DarkSanadColors.bg,
    onBackground = DarkSanadColors.text,
    surface = DarkSanadColors.surface,
    onSurface = DarkSanadColors.text,
    surfaceVariant = DarkSanadColors.surface,
    onSurfaceVariant = DarkSanadColors.textSecondary,
    outline = DarkSanadColors.divider,
    outlineVariant = DarkSanadColors.borderInput,
    error = DarkSanadColors.danger,
    onError = DarkSanadColors.onAccent,
    errorContainer = DarkSanadColors.surface,
    onErrorContainer = DarkSanadColors.danger
)

private val SanadLightColorScheme = lightColorScheme(
    primary = LightSanadColors.accent,
    onPrimary = LightSanadColors.onAccent,
    primaryContainer = LightSanadColors.surface,
    onPrimaryContainer = LightSanadColors.text,
    secondary = LightSanadColors.accentStrong,
    onSecondary = LightSanadColors.onAccent,
    secondaryContainer = LightSanadColors.divider,
    onSecondaryContainer = LightSanadColors.text,
    tertiary = LightSanadColors.busy,
    onTertiary = LightSanadColors.onAccent,
    tertiaryContainer = LightSanadColors.surface,
    onTertiaryContainer = LightSanadColors.text,
    background = LightSanadColors.bg,
    onBackground = LightSanadColors.text,
    surface = LightSanadColors.surface,
    onSurface = LightSanadColors.text,
    surfaceVariant = LightSanadColors.surface,
    onSurfaceVariant = LightSanadColors.textSecondary,
    outline = LightSanadColors.divider,
    outlineVariant = LightSanadColors.borderInput,
    error = LightSanadColors.danger,
    onError = LightSanadColors.onAccent,
    errorContainer = LightSanadColors.surface,
    onErrorContainer = LightSanadColors.danger
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Dark mode is default
    dynamicColor: Boolean = false, // Preserve brand identity
    content: @Composable () -> Unit,
) {
    val sanadColors = if (darkTheme) DarkSanadColors else LightSanadColors
    val colorScheme = if (darkTheme) SanadDarkColorScheme else SanadLightColorScheme

    CompositionLocalProvider(
        LocalSanadColors provides sanadColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SanadTypography,
            content = content
        )
    }
}
