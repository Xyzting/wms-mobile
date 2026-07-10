package com.utb.wms.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SkemaTerang = lightColorScheme(
    primary = BiruUtama,
    onPrimary = Putih,
    primaryContainer = BiruKontainer,
    onPrimaryContainer = BiruGelap,
    secondary = JinggaAksen,
    onSecondary = Putih,
    secondaryContainer = JinggaKontainer,
    onSecondaryContainer = BiruGelap,
    tertiary = HijauAman,
    onTertiary = Putih,
    tertiaryContainer = HijauKontainer,
    onTertiaryContainer = HijauGelap,
    error = MerahGalat,
    onError = Putih,
    errorContainer = MerahKontainer,
    onErrorContainer = MerahGalat,
    background = AbuLatar,
    onBackground = BiruGelap,
    surface = AbuPermukaan,
    onSurface = BiruGelap,
    surfaceVariant = AbuPermukaanVarian,
    onSurfaceVariant = AbuTeks,
    outline = AbuGaris,
    outlineVariant = AbuGarisRedup,
    surfaceDim = AbuPermukaanRedup,
    surfaceBright = Putih,
    surfaceContainerLowest = Putih,
    surfaceContainerLow = AbuPermukaanRendah,
    surfaceContainer = AbuPermukaanKontainer,
    surfaceContainerHigh = AbuPermukaanTinggi,
    surfaceContainerHighest = AbuPermukaanTertinggi,
    inverseSurface = AbuPermukaanTerbalik,
    inverseOnSurface = AbuTeksTerbalik,
    inversePrimary = BiruTerang,
)

private val SkemaGelap = darkColorScheme(
    primary = BiruTerang,
    onPrimary = BiruGelap,
    primaryContainer = BiruKontainerGelap,
    onPrimaryContainer = BiruKontainer,
    secondary = JinggaTerang,
    onSecondary = JinggaGelap,
    secondaryContainer = JinggaKontainerGelap,
    onSecondaryContainer = JinggaKontainer,
    tertiary = HijauTerang,
    onTertiary = HijauPalingGelap,
    tertiaryContainer = HijauKontainerGelap,
    onTertiaryContainer = HijauKontainer,
    error = MerahTerang,
    onError = MerahGelap,
    errorContainer = MerahKontainerGelap,
    onErrorContainer = MerahKontainer,
    background = MalamLatar,
    onBackground = MalamTeks,
    surface = MalamPermukaan,
    onSurface = MalamTeks,
    surfaceVariant = MalamPermukaanVarian,
    onSurfaceVariant = MalamTeksVarian,
    outline = MalamGaris,
    outlineVariant = MalamGarisRedup,
    surfaceDim = MalamLatar,
    surfaceBright = MalamPermukaanTerang,
    surfaceContainerLowest = MalamPermukaanTerendah,
    surfaceContainerLow = MalamPermukaan,
    surfaceContainer = MalamPermukaanKontainer,
    surfaceContainerHigh = MalamPermukaanTinggi,
    surfaceContainerHighest = MalamPermukaanTertinggi,
    inverseSurface = MalamTeks,
    inverseOnSurface = MalamPermukaanVarian,
    inversePrimary = BiruUtama,
)

@Composable
fun WMSMobileTheme(
    gunakanTemaGelap: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (gunakanTemaGelap) SkemaGelap else SkemaTerang,
        typography = WmsTypography,
        shapes = WmsShapes,
        content = content,
    )
}
