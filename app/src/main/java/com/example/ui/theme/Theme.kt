package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val LocalIsDarkTheme = staticCompositionLocalOf { false }

data class StudySemanticColors(
    val offlineGreen: Color,
    val offlineContainer: Color,
    val noticeAmber: Color,
    val noticeContainer: Color
)

@Composable
fun studySemanticColors(): StudySemanticColors {
    val isDark = LocalIsDarkTheme.current
    return if (isDark) {
        StudySemanticColors(
            offlineGreen = OfflineSuccessGreenDark,
            offlineContainer = OfflineSuccessContainerDark,
            noticeAmber = SampleNoticeAmberDark,
            noticeContainer = SampleNoticeContainerDark
        )
    } else {
        StudySemanticColors(
            offlineGreen = OfflineSuccessGreen,
            offlineContainer = OfflineSuccessContainer,
            noticeAmber = SampleNoticeAmber,
            noticeContainer = SampleNoticeContainer
        )
    }
}

private val LightColorScheme = lightColorScheme(
    primary = DeepTeal,
    onPrimary = OnDeepTeal,
    primaryContainer = TealContainer,
    onPrimaryContainer = OnTealContainer,
    secondary = WarmAmber,
    onSecondary = OnWarmAmber,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = OnAmberContainer,
    tertiary = IndigoCobalt,
    onTertiary = OnIndigoCobalt,
    tertiaryContainer = CobaltContainer,
    onTertiaryContainer = OnCobaltContainer,
    background = ParchmentBackground,
    onBackground = InkCharcoal,
    surface = ParchmentSurface,
    onSurface = InkCharcoal,
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = InkMuted,
    outline = ParchmentOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = MintTealDark,
    onPrimary = OnMintTealDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = OnTealContainerDark,
    secondary = SoftAmberDark,
    onSecondary = OnSoftAmberDark,
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = OnAmberContainerDark,
    tertiary = SoftCobaltDark,
    onTertiary = OnSoftCobaltDark,
    tertiaryContainer = CobaltContainerDark,
    onTertiaryContainer = OnCobaltContainerDark,
    background = ObsidianBackground,
    onBackground = MistWhite,
    surface = ObsidianSurface,
    onSurface = MistWhite,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = MistMuted,
    outline = ObsidianOutline
)

val EduLensShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun EduLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = EduLensShapes,
            content = content
        )
    }
}
