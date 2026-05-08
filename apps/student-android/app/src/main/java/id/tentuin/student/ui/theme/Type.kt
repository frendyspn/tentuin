package id.tentuin.student.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import id.tentuin.student.R
import androidx.compose.ui.text.font.FontFamily

val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val plusJakartaSans = GoogleFont("Plus Jakarta Sans")

val PlusJakartaSansFamily = FontFamily(
    Font(googleFont = plusJakartaSans, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = plusJakartaSans, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = plusJakartaSans, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = plusJakartaSans, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = plusJakartaSans, fontProvider = googleFontProvider, weight = FontWeight.ExtraBold),
)

val TentuinTypography = Typography(
    displayLarge  = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 40.sp),
    displayMedium = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold,      fontSize = 28.sp, lineHeight = 36.sp),
    headlineLarge = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold,      fontSize = 24.sp, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp, lineHeight = 26.sp),
    titleLarge    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold,      fontSize = 16.sp, lineHeight = 24.sp),
    titleMedium   = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 14.sp, lineHeight = 20.sp),
    titleSmall    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium,    fontSize = 13.sp, lineHeight = 18.sp),
    bodyLarge     = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium   = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium,    fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall    = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Medium,    fontSize = 11.sp, lineHeight = 14.sp),
)
