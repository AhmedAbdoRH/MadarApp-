package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Sanad Brand Design Tokens (Single Source of Truth)
data class SanadColors(
    val bg: Color,
    val surface: Color,
    val divider: Color,
    val text: Color,
    val textSecondary: Color,
    val borderInput: Color,
    val accent: Color,
    val accentStrong: Color,
    val onAccent: Color,
    val bubbleUser: Color,
    val online: Color,
    val busy: Color,
    val danger: Color
)

// Dark Theme (Default)
val DarkSanadColors = SanadColors(
    bg = Color(0xFF0A1F2E),
    surface = Color(0xFF123047),
    divider = Color(0xFF1E4560),
    text = Color(0xFFF5EFE3),
    textSecondary = Color(0xFFB8C4CC),
    borderInput = Color(0xFF5F7F94),
    accent = Color(0xFFE07A5F),
    accentStrong = Color(0xFFE07A5F),
    onAccent = Color(0xFF0A1F2E),
    bubbleUser = Color(0xFF1B4F72),
    online = Color(0xFF3FA37A),
    busy = Color(0xFFD9A441),
    danger = Color(0xFFE26D6D)
)

// Light Theme (Defined for future use, not default)
val LightSanadColors = SanadColors(
    bg = Color(0xFFF5EFE3),
    surface = Color(0xFFEBE3D2),
    divider = Color(0xFFD9CFBA),
    text = Color(0xFF0B4262),
    textSecondary = Color(0xFF4A6B80),
    borderInput = Color(0xFF8A7D66),
    accent = Color(0xFFC8553D),
    accentStrong = Color(0xFFA8432F),
    onAccent = Color(0xFFFFFFFF),
    bubbleUser = Color(0xFF0B4262),
    online = Color(0xFF2E7D5B),
    busy = Color(0xFFB7791F),
    danger = Color(0xFF9B2C2C)
)

val LocalSanadColors = staticCompositionLocalOf { DarkSanadColors }

object SanadTheme {
    val colors: SanadColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSanadColors.current
}

// Backward-compatibility aliases mapped strictly to brand tokens
val SanadTealPrimary = DarkSanadColors.accent
val SanadTealDark = DarkSanadColors.text
val SanadTealSecondary = DarkSanadColors.textSecondary
val SanadTealContainer = DarkSanadColors.surface
val SanadOnTealContainer = DarkSanadColors.text
val SanadOnlineGreen = DarkSanadColors.online
val SanadOnlineGreenBg = DarkSanadColors.surface
val SanadUrgentRed = DarkSanadColors.danger
val SanadUrgentContainer = DarkSanadColors.surface
val SanadAmber = DarkSanadColors.busy
val SanadAmberContainer = DarkSanadColors.surface
val SanadTextPrimary = DarkSanadColors.text
val SanadTextSecondary = DarkSanadColors.textSecondary
val SanadTextMuted = DarkSanadColors.textSecondary
val SanadCardBg = DarkSanadColors.surface
val SanadCardBorder = DarkSanadColors.divider
val SanadWarmSand = DarkSanadColors.bg
val SanadCream = DarkSanadColors.bg
val SanadSage = DarkSanadColors.online
val SanadSageContainer = DarkSanadColors.surface
