package com.oscar.orbimusic.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.oscar.orbimusic.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val BricolageFontName = GoogleFont("Bricolage Grotesque")

val BricolageFontFamily = FontFamily(
    Font(googleFont = BricolageFontName, fontProvider = provider),
    Font(googleFont = BricolageFontName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = BricolageFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = BricolageFontName, fontProvider = provider, weight = FontWeight.SemiBold)
)

private val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = BricolageFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = BricolageFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = BricolageFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = BricolageFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = BricolageFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = BricolageFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = BricolageFontFamily, fontWeight = FontWeight.Bold),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = BricolageFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = BricolageFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = BricolageFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = BricolageFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = BricolageFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = BricolageFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = BricolageFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = BricolageFontFamily)
)