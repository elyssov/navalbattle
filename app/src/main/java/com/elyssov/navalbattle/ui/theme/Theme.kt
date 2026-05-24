package com.elyssov.navalbattle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NavalColorScheme = darkColorScheme(
    primary = SeaPrimary,
    onPrimary = MissWhite,
    secondary = SeaAccent,
    onSecondary = SunkBlack,
    tertiary = RadarGreen,
    background = SeaBackground,
    onBackground = MissWhite,
    surface = SeaSurface,
    onSurface = MissWhite,
    error = HitRed,
    onError = MissWhite
)

@Composable
fun NavalBattleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NavalColorScheme,
        typography = NavalTypography,
        content = content
    )
}
