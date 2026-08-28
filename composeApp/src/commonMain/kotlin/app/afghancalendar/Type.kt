package app.afghancalendar

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import afghan_calendar.composeapp.generated.resources.Res
import afghan_calendar.composeapp.generated.resources.Inter_Bold
import afghan_calendar.composeapp.generated.resources.Inter_Medium
import afghan_calendar.composeapp.generated.resources.Inter_Regular
import afghan_calendar.composeapp.generated.resources.Vazirmatn_Bold
import afghan_calendar.composeapp.generated.resources.Vazirmatn_Medium
import afghan_calendar.composeapp.generated.resources.Vazirmatn_Regular
import org.jetbrains.compose.resources.Font

// Vazirmatn for Persian/Dari/Pashto/Arabic - embedded via composeResources/font (ttf/otf)
@Composable
fun VazirmatnFontFamily(): FontFamily = FontFamily(
    Font(Res.font.Vazirmatn_Regular, FontWeight.Normal),
    Font(Res.font.Vazirmatn_Medium, FontWeight.Medium),
    Font(Res.font.Vazirmatn_Bold, FontWeight.Bold)
)

// Inter for English/Latin - clean geometric sans-serif, embedded via composeResources/font
@Composable
fun InterFontFamily(): FontFamily = FontFamily(
    Font(Res.font.Inter_Regular, FontWeight.Normal),
    Font(Res.font.Inter_Medium, FontWeight.Medium),
    Font(Res.font.Inter_Bold, FontWeight.Bold)
)

// Default Persian typography - Vazirmatn
@Composable
fun AppTypography(): Typography {
    val vazirmatn = VazirmatnFontFamily()
    return Typography(
    displayLarge = Typography().displayLarge.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
    displayMedium = Typography().displayMedium.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Bold),
    displaySmall = Typography().displaySmall.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Medium),
    headlineLarge = Typography().headlineLarge.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Medium),
    headlineMedium = Typography().headlineMedium.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Medium),
    headlineSmall = Typography().headlineSmall.copy(fontFamily = vazirmatn, fontWeight = FontWeight.SemiBold),
    titleLarge = Typography().titleLarge.copy(fontFamily = vazirmatn, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = Typography().titleMedium.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    titleSmall = Typography().titleSmall.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Medium, letterSpacing = 0.1.sp),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Normal),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Normal),
    bodySmall = Typography().bodySmall.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Normal),
    labelLarge = Typography().labelLarge.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Medium),
    labelMedium = Typography().labelMedium.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Medium),
    labelSmall = Typography().labelSmall.copy(fontFamily = vazirmatn, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
    )
}
