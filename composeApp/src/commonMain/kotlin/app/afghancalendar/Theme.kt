package app.afghancalendar

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Afghan palettes - 5 seeds
object AfghanSeeds {
    val Yellow = Color(0xFFFDB813)      // Afghan Yellow default
    val Lapis = Color(0xFF26428B)       // Lapis Lazuli
    val Pomegranate = Color(0xFFC1272D) // Pomegranate
    val Teal = Color(0xFF005F5B)        // Deep Teal
    val Desert = Color(0xFF8D6E4A)      // Desert Sand
}

data class SeedOption(
    val nameEn: String,
    val nameFa: String,
    val color: Color
)

val seedOptions = listOf(
    SeedOption("Afghan Yellow", "زرد افغان", AfghanSeeds.Yellow),
    SeedOption("Lapis Lazuli", "لاجورد", AfghanSeeds.Lapis),
    SeedOption("Pomegranate", "انار", AfghanSeeds.Pomegranate),
    SeedOption("Deep Teal", "سبز تیره", AfghanSeeds.Teal),
    SeedOption("Desert Sand", "ریگ صحرا", AfghanSeeds.Desert),
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

// Hand-crafted schemes per seed - expressive tonal surfaces
// We keep primaryContainer = seed for saturated month card (expressive look)
// onPrimaryContainer chosen for contrast (dark text on light yellow, white on dark seeds)

fun lightSchemeForSeed(seed: Color): ColorScheme {
    return when (seed) {
        AfghanSeeds.Yellow -> lightColorScheme(
            primary = Color(0xFF7A5900),
            onPrimary = Color.White,
            primaryContainer = AfghanSeeds.Yellow,
            onPrimaryContainer = Color(0xFF221900),
            secondary = Color(0xFF705D2E),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFE08D),
            onSecondaryContainer = Color(0xFF221B00),
            tertiary = Color(0xFF4B6545),
            surface = Color(0xFFFFF8F0),
            onSurface = Color(0xFF1F1B13),
            surfaceVariant = Color(0xFFF0E0C2),
            onSurfaceVariant = Color(0xFF4D4639),
            surfaceContainer = Color(0xFFF5EEE0),
            surfaceContainerHigh = Color(0xFFF0E8D8),
            surfaceContainerHighest = Color(0xFFEAE2D0),
            outline = Color(0xFF7D7767),
            outlineVariant = Color(0xFFD0C5B4)
        )
        AfghanSeeds.Lapis -> lightColorScheme(
            primary = AfghanSeeds.Lapis,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDCE1FF),
            onPrimaryContainer = Color(0xFF001552),
            secondary = Color(0xFF5A5D72),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFDEE1F9),
            tertiary = Color(0xFF705573),
            surface = Color(0xFFFEFBFF),
            onSurface = Color(0xFF1B1B1F),
            surfaceVariant = Color(0xFFE1E2EC),
            onSurfaceVariant = Color(0xFF44464F),
            surfaceContainer = Color(0xFFF0F0F7),
            surfaceContainerHigh = Color(0xFFE9E9F0),
            surfaceContainerHighest = Color(0xFFE3E2E9),
            outline = Color(0xFF757780),
            outlineVariant = Color(0xFFC5C6D0)
        )
        AfghanSeeds.Pomegranate -> lightColorScheme(
            primary = AfghanSeeds.Pomegranate,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFDAD6),
            onPrimaryContainer = Color(0xFF410002),
            secondary = Color(0xFF775651),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFDAD6),
            tertiary = Color(0xFF715C2E),
            surface = Color(0xFFFFFBFF),
            onSurface = Color(0xFF22191A),
            surfaceVariant = Color(0xFFF5DDDB),
            onSurfaceVariant = Color(0xFF534341),
            surfaceContainer = Color(0xFFFBEAEA),
            surfaceContainerHigh = Color(0xFFF5E4E3),
            surfaceContainerHighest = Color(0xFFEEDAD8),
            outline = Color(0xFF857370),
            outlineVariant = Color(0xFFD8C2BF)
        )
        AfghanSeeds.Teal -> lightColorScheme(
            primary = AfghanSeeds.Teal,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF9CF1E6),
            onPrimaryContainer = Color(0xFF00201D),
            secondary = Color(0xFF4A635F),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFCCE8E3),
            tertiary = Color(0xFF4E6380),
            surface = Color(0xFFF6FAF8),
            onSurface = Color(0xFF171D1B),
            surfaceVariant = Color(0xFFDBE5E0),
            onSurfaceVariant = Color(0xFF3F4946),
            surfaceContainer = Color(0xFFEBEFED),
            surfaceContainerHigh = Color(0xFFE5E9E7),
            surfaceContainerHighest = Color(0xFFDFE4E1),
            outline = Color(0xFF6F7976),
            outlineVariant = Color(0xFFBFC9C5)
        )
        AfghanSeeds.Desert -> lightColorScheme(
            primary = Color(0xFF6B4A2E),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFDBC0),
            onPrimaryContainer = Color(0xFF2B1700),
            secondary = Color(0xFF705B41),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFADCC6),
            tertiary = Color(0xFF5A6238),
            surface = Color(0xFFFFF8F4),
            onSurface = Color(0xFF201A12),
            surfaceVariant = Color(0xFFF0DFD2),
            onSurfaceVariant = Color(0xFF50443A),
            surfaceContainer = Color(0xFFF8ECDF),
            surfaceContainerHigh = Color(0xFFF2E6D8),
            surfaceContainerHighest = Color(0xFFECE0D0),
            outline = Color(0xFF827568),
            outlineVariant = Color(0xFFD5C4B3)
        )
        else -> lightColorScheme(primary = seed, primaryContainer = seed)
    }
}

fun darkSchemeForSeed(seed: Color): ColorScheme {
    return when (seed) {
        AfghanSeeds.Yellow -> darkColorScheme(
            primary = Color(0xFFFFD86B),
            onPrimary = Color(0xFF3E2E00),
            primaryContainer = Color(0xFF5C4300),
            onPrimaryContainer = Color(0xFFFFDE9C),
            secondary = Color(0xFFE2C16C),
            onSecondary = Color(0xFF3E2E00),
            secondaryContainer = Color(0xFF564500),
            tertiary = Color(0xFFBFC9AC),
            surface = Color(0xFF15130C),
            onSurface = Color(0xFFEAE1CF),
            surfaceVariant = Color(0xFF4D4639),
            onSurfaceVariant = Color(0xFFD0C5B4),
            surfaceContainer = Color(0xFF211F16),
            surfaceContainerHigh = Color(0xFF2C2A1F),
            surfaceContainerHighest = Color(0xFF373529),
            outline = Color(0xFF9A8F80),
            outlineVariant = Color(0xFF4D4639)
        )
        AfghanSeeds.Lapis -> darkColorScheme(
            primary = Color(0xFFB3C5FF),
            onPrimary = Color(0xFF00287B),
            primaryContainer = Color(0xFF00319E),
            onPrimaryContainer = Color(0xFFDCE1FF),
            secondary = Color(0xFFC2C5DD),
            onSecondary = Color(0xFF2B3042),
            secondaryContainer = Color(0xFF41465A),
            tertiary = Color(0xFFDAB9D5),
            surface = Color(0xFF131316),
            onSurface = Color(0xFFE4E2E6),
            surfaceVariant = Color(0xFF44464F),
            onSurfaceVariant = Color(0xFFC5C6D0),
            surfaceContainer = Color(0xFF1F1F23),
            surfaceContainerHigh = Color(0xFF2A2A2E),
            surfaceContainerHighest = Color(0xFF343438),
            outline = Color(0xFF8F9099),
            outlineVariant = Color(0xFF44464F)
        )
        AfghanSeeds.Pomegranate -> darkColorScheme(
            primary = Color(0xFFFFB4AB),
            onPrimary = Color(0xFF690005),
            primaryContainer = Color(0xFF93000A),
            onPrimaryContainer = Color(0xFFFFDAD6),
            secondary = Color(0xFFE7BDB7),
            onSecondary = Color(0xFF442926),
            secondaryContainer = Color(0xFF5D3F3C),
            tertiary = Color(0xFFE3C08C),
            surface = Color(0xFF1A1111),
            onSurface = Color(0xFFF1DEDD),
            surfaceVariant = Color(0xFF534341),
            onSurfaceVariant = Color(0xFFD8C2BF),
            surfaceContainer = Color(0xFF271414),
            surfaceContainerHigh = Color(0xFF322020),
            surfaceContainerHighest = Color(0xFF3D2A29),
            outline = Color(0xFFA08C8A),
            outlineVariant = Color(0xFF534341)
        )
        AfghanSeeds.Teal -> darkColorScheme(
            primary = Color(0xFF80D5CB),
            onPrimary = Color(0xFF003731),
            primaryContainer = Color(0xFF00504A),
            onPrimaryContainer = Color(0xFF9CF1E6),
            secondary = Color(0xFFB0CCC7),
            onSecondary = Color(0xFF1C3531),
            secondaryContainer = Color(0xFF324B47),
            tertiary = Color(0xFFB4C8E8),
            surface = Color(0xFF0F1514),
            onSurface = Color(0xFFDEE4E0),
            surfaceVariant = Color(0xFF3F4946),
            onSurfaceVariant = Color(0xFFBFC9C5),
            surfaceContainer = Color(0xFF1B2120),
            surfaceContainerHigh = Color(0xFF252B2A),
            surfaceContainerHighest = Color(0xFF303635),
            outline = Color(0xFF899390),
            outlineVariant = Color(0xFF3F4946)
        )
        AfghanSeeds.Desert -> darkColorScheme(
            primary = Color(0xFFDEB68E),
            onPrimary = Color(0xFF3E1F00),
            primaryContainer = Color(0xFF5A3A1B),
            onPrimaryContainer = Color(0xFFFFDBC0),
            secondary = Color(0xFFD8BDA3),
            onSecondary = Color(0xFF3E2D1E),
            secondaryContainer = Color(0xFF564333),
            tertiary = Color(0xFFC4CA9A),
            surface = Color(0xFF17130E),
            onSurface = Color(0xFFECE0D0),
            surfaceVariant = Color(0xFF50443A),
            onSurfaceVariant = Color(0xFFD5C4B3),
            surfaceContainer = Color(0xFF242017),
            surfaceContainerHigh = Color(0xFF2F2A20),
            surfaceContainerHighest = Color(0xFF3A352B),
            outline = Color(0xFF9E8E7F),
            outlineVariant = Color(0xFF50443A)
        )
        else -> darkColorScheme(primary = seed)
    }
}

fun getColorScheme(seedIndex: Int, isDark: Boolean): ColorScheme {
    val seed = seedOptions.getOrNull(seedIndex)?.color ?: AfghanSeeds.Yellow
    return if (isDark) darkSchemeForSeed(seed) else lightSchemeForSeed(seed)
}

// Expressive shapes: extraLarge 28dp, extraLargeIncreased 32dp (fallback to extraLarge if not available)
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
val ExtraLargeIncreasedShape = RoundedCornerShape(32.dp)
val LargeIncreasedShape = RoundedCornerShape(20.dp)
