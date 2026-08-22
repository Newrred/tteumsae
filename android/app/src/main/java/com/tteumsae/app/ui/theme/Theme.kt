package com.tteumsae.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.tteumsae.app.R

val TteumRed = Color(0xFFE60F33)
val TteumRedSoft = TteumRed.copy(alpha = 0.10f)
val TteumInk = Color(0xFF171719)
val TteumMuted = Color(0xFF6B7079)
val TteumSurface = Color(0xFFF5F6F8)

private val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

private fun TextStyle.withPretendard() = copy(fontFamily = Pretendard)

private val DefaultTypography = Typography()
private val TteumsaeTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.withPretendard(),
    displayMedium = DefaultTypography.displayMedium.withPretendard(),
    displaySmall = DefaultTypography.displaySmall.withPretendard(),
    headlineLarge = DefaultTypography.headlineLarge.withPretendard(),
    headlineMedium = DefaultTypography.headlineMedium.withPretendard(),
    headlineSmall = DefaultTypography.headlineSmall.withPretendard(),
    titleLarge = DefaultTypography.titleLarge.withPretendard(),
    titleMedium = DefaultTypography.titleMedium.withPretendard(),
    titleSmall = DefaultTypography.titleSmall.withPretendard(),
    bodyLarge = DefaultTypography.bodyLarge.withPretendard(),
    bodyMedium = DefaultTypography.bodyMedium.withPretendard(),
    bodySmall = DefaultTypography.bodySmall.withPretendard(),
    labelLarge = DefaultTypography.labelLarge.withPretendard(),
    labelMedium = DefaultTypography.labelMedium.withPretendard(),
    labelSmall = DefaultTypography.labelSmall.withPretendard(),
)

private val TteumsaeColors = lightColorScheme(
    primary = TteumRed,
    onPrimary = Color.White,
    secondary = TteumInk,
    background = Color(0xFFF7F7F4),
    surface = Color.White,
    onSurface = TteumInk,
    surfaceVariant = TteumSurface,
    onSurfaceVariant = TteumMuted,
    outline = Color(0xFFE1E3E8),
)

@Composable
fun TteumsaeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TteumsaeColors,
        typography = TteumsaeTypography,
        content = content,
    )
}
