package com.utb.wms.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SkemaTerang = lightColorScheme(
    primary = BiruUtama,
    onPrimary = Color.White,
    primaryContainer = BiruKontainer,
    onPrimaryContainer = BiruGelap,
    secondary = JinggaAksen,
    onSecondary = Color.White,
    secondaryContainer = JinggaKontainer,
    onSecondaryContainer = BiruGelap,
    background = AbuLatar,
    onBackground = BiruGelap,
    surface = AbuPermukaan,
    onSurface = BiruGelap,
    onSurfaceVariant = AbuTeks,
    error = MerahGalat,
    onError = Color.White,
    errorContainer = MerahKontainer,
    onErrorContainer = MerahGalat,
)

private val SkemaGelap = darkColorScheme(
    primary = BiruTerang,
    onPrimary = BiruGelap,
    primaryContainer = BiruGelap,
    onPrimaryContainer = BiruKontainer,
    secondary = JinggaAksen,
    onSecondary = Color.Black,
    error = MerahGalat,
    onError = Color.White,
)

@Composable
fun WMSMobileTheme(
    gunakanTemaGelap: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (gunakanTemaGelap) SkemaGelap else SkemaTerang,
        typography = WmsTypography,
        content = content,
    )
}
