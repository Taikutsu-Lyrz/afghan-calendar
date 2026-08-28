package app.afghancalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Pager window: pages centered on current month
private const val PAGER_PAGE_COUNT = 24000
private const val PAGER_INITIAL_PAGE = 12000

// Maps pager page index -> (Jalali year, month), relative to the start month
private fun pageToJalaliMonth(page: Int, startYear: Int, startMonth: Int): Pair<Int, Int> {
    val totalMonths = startYear * 12 + (startMonth - 1) + (page - PAGER_INITIAL_PAGE)
    return (totalMonths / 12) to (totalMonths % 12 + 1)
}

// Maps pager page index -> (Gregorian year, month), relative to the start month
private fun pageToGregorianMonth(page: Int, startYear: Int, startMonth: Int): Pair<Int, Int> {
    var totalMonths = startYear * 12 + (startMonth - 1) + (page - PAGER_INITIAL_PAGE)
    if (totalMonths < 1) totalMonths = 1
    return (totalMonths / 12) to (totalMonths % 12 + 1)
}

// Maps pager page index -> (Hijri year, month), relative to today's Hijri month
private fun pageToHijriMonth(page: Int, startYear: Int, startMonth: Int): Pair<Int, Int> {
    var totalMonths = startYear * 12 + (startMonth - 1) + (page - PAGER_INITIAL_PAGE)
    if (totalMonths < 144) totalMonths = 144 // Hijri year >= 12
    return (totalMonths / 12) to (totalMonths % 12 + 1)
}

// Week start options - user selectable
enum class WeekStart(val startIdx: Int, val labelFa: String, val labelEn: String) {
    SATURDAY(6, "شنبه", "Saturday"),
    SUNDAY(0, "یکشنبه", "Sunday"),
    MONDAY(1, "دوشنبه", "Monday")
}

// Localized UI strings
private class AppStrings(
    val appName: String,
    val settings: String,
    val today: String,
    val previous: String,
    val next: String,
    val mainCalendar: String,
    val shamsi: String,
    val miladi: String,
    val hijri: String,
    val language: String,
    val themeColor: String,
    val appearance: String,
    val light: String,
    val system: String,
    val dark: String,
    val weekStart: String,
    val offlineNote: String,
    val close: String
)

private fun stringsFor(language: AppLanguage): AppStrings = if (language == AppLanguage.ENGLISH) AppStrings(
    appName = "Afghan Calendar",
    settings = "Settings",
    today = "Back to today",
    previous = "Previous",
    next = "Next",
    mainCalendar = "Main calendar",
    shamsi = "Shamsi",
    miladi = "Miladi",
    hijri = "Hijri",
    language = "Language",
    themeColor = "Theme color",
    appearance = "Appearance",
    light = "Light",
    system = "System",
    dark = "Dark",
    weekStart = "Week start",
    offlineNote = "Local calendar — works offline.",
    close = "Close"
) else AppStrings(
    appName = "تقویم افغان",
    settings = "تنظیمات",
    today = "بازگشت به امروز",
    previous = "ماه قبل",
    next = "ماه بعد",
    mainCalendar = "تقویم اصلی",
    shamsi = "شمسی",
    miladi = "میلادی",
    hijri = "هجری",
    language = "زبان",
    themeColor = "رنگ پوسته",
    appearance = "حالت نمایش",
    light = "روشن",
    system = "سیستم",
    dark = "تاریک",
    weekStart = "شروع هفته",
    offlineNote = "تقویم محلی — بدون اینترنت کار می‌کند.",
    close = "بستن"
)

// One date column (day / month / year stacked) for the detail card
@Composable
private fun RowScope.DateColumn(
    day: String,
    month: String,
    year: String,
    font: FontFamily,
    onSurface: Color
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = day,
            color = onSurface,
            fontFamily = font,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = month,
            color = onSurface,
            fontFamily = font,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(
            text = year,
            color = onSurface,
            fontFamily = font,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    // Theme state - persisted via rememberSaveable
    var seedIndex by rememberSaveable { mutableStateOf(0) }
    var themeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }
    var weekStart by rememberSaveable { mutableStateOf(WeekStart.SATURDAY) }
    var language by rememberSaveable { mutableStateOf(AppLanguage.PERSIAN) }
    var mainCalendar by rememberSaveable { mutableStateOf(MainCalendar.SHAMSI) }
    var showSettings by remember { mutableStateOf(false) }

    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemDark
    }
    val colorScheme = remember(seedIndex, isDark) { getColorScheme(seedIndex, isDark) }

    val isEnglish = language == AppLanguage.ENGLISH
    val isShamsi = mainCalendar == MainCalendar.SHAMSI
    val s = remember(language) { stringsFor(language) }

    // Typography: build once per language (not on every recomposition)
    val interFont = InterFontFamily()
    val vazirFont = VazirmatnFontFamily()
    val typography = remember(isEnglish) { AppTypography(if (isEnglish) interFont else vazirFont) }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = AppShapes
    ) {
        val todayGregorian = remember { getTodayGregorian() }
        val todayJalali = remember { gregorianToJalali(todayGregorian) }
        val todayHijri = remember { gregorianToHijri(todayGregorian) }
        val todayJdn = remember { gregorianToJdn(todayGregorian.year, todayGregorian.month, todayGregorian.day) }

        // Selection stored as JDN - one source of truth for all calendars
        var selectedJdn by rememberSaveable { mutableStateOf(todayJdn) }

        val pagerState = rememberPagerState(initialPage = PAGER_INITIAL_PAGE) { PAGER_PAGE_COUNT }
        val scope = rememberCoroutineScope()

        fun goToday() {
            selectedJdn = todayJdn
            scope.launch { pagerState.animateScrollToPage(PAGER_INITIAL_PAGE) }
        }

        Scaffold(
            containerColor = colorScheme.surface,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            s.appName,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { goToday() }) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = s.today,
                                modifier = Modifier.padding(start = 4.dp).size(26.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = s.settings)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = colorScheme.surface,
                        titleContentColor = colorScheme.onSurface,
                        navigationIconContentColor = colorScheme.onSurface,
                        actionIconContentColor = colorScheme.onSurface
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 480.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MonthCard(
                        pagerState = pagerState,
                        scope = scope,
                        selectedJdn = selectedJdn,
                        onDaySelected = { selectedJdn = it },
                        weekStart = weekStart,
                        mainCalendar = mainCalendar,
                        language = language,
                        todayJdn = todayJdn,
                        todayJalaliYear = todayJalali.year,
                        todayJalaliMonth = todayJalali.month,
                        todayGregorianYear = todayGregorian.year,
                        todayGregorianMonth = todayGregorian.month,
                        todayHijriYear = todayHijri.year,
                        todayHijriMonth = todayHijri.month
                    )
                    DetailCard(
                        selectedJdn = selectedJdn,
                        language = language
                    )
                }
            }
        }

        if (showSettings) {
            SettingsDialog(
                seedIndex = seedIndex,
                onSeedIndexChange = { seedIndex = it },
                themeMode = themeMode,
                onThemeModeChange = { themeMode = it },
                weekStart = weekStart,
                onWeekStartChange = { weekStart = it },
                language = language,
                onLanguageChange = { language = it },
                mainCalendar = mainCalendar,
                onMainCalendarChange = { mainCalendar = it },
                strings = s,
                onDismiss = { showSettings = false }
            )
        }
    }
}

// === Month Card - primaryContainer tonal, swipeable grid ===
@Composable
private fun MonthCard(
    pagerState: PagerState,
    scope: kotlinx.coroutines.CoroutineScope,
    selectedJdn: Int,
    onDaySelected: (Int) -> Unit,
    weekStart: WeekStart,
    mainCalendar: MainCalendar,
    language: AppLanguage,
    todayJdn: Int,
    todayJalaliYear: Int,
    todayJalaliMonth: Int,
    todayGregorianYear: Int,
    todayGregorianMonth: Int,
    todayHijriYear: Int,
    todayHijriMonth: Int
) {
    val colorScheme = MaterialTheme.colorScheme
    val isEnglish = language == AppLanguage.ENGLISH
    val isShamsi = mainCalendar == MainCalendar.SHAMSI
    val isHijri = mainCalendar == MainCalendar.HIJRI
    val vazirmatnFontFamily = VazirmatnFontFamily()
    val interFontFamily = InterFontFamily()
    val s = remember(language) { stringsFor(language) }

    // settledPage is read HERE so only this card recomposes when a swipe settles
    val (displayedYear, displayedMonth) = remember(pagerState.settledPage, mainCalendar) {
        when (mainCalendar) {
            MainCalendar.SHAMSI -> pageToJalaliMonth(pagerState.settledPage, todayJalaliYear, todayJalaliMonth)
            MainCalendar.MILADI -> pageToGregorianMonth(pagerState.settledPage, todayGregorianYear, todayGregorianMonth)
            MainCalendar.HIJRI -> pageToHijriMonth(pagerState.settledPage, todayHijriYear, todayHijriMonth)
        }
    }

    fun prevMonth() { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
    fun nextMonth() { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }

    // Weekday headers rotate with the selected week start
    val weekdayHeaders: List<String> = remember(weekStart, isShamsi, isEnglish) {
        val persianFull = arrayOf("شنبه","یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه") // Sat-first order
        val start = weekStart.startIdx // Sun-based index: Sat=6, Sun=0, Mon=1
        val orderedPersian = (0 until 7).map { i ->
            val idx = ((start % 7) + 1 + i) % 7 // convert Sun-based to Sat-based rotation
            persianFull[idx]
        }
        val englishShort = arrayOf("Sat","Sun","Mon","Tue","Wed","Thu","Fri") // Sat-first order
        val orderedEnglish = (0 until 7).map { i ->
            val idx = ((start % 7) + 1 + i) % 7
            englishShort[idx]
        }
        when {
            (isShamsi || isHijri) && !isEnglish -> orderedPersian
            else -> orderedEnglish
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = ExtraLargeIncreasedShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month navigator
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledTonalIconButton(
                    onClick = { prevMonth() },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = colorScheme.surface.copy(alpha = 0.85f),
                        contentColor = colorScheme.onSurface
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = s.previous, modifier = Modifier.size(24.dp))
                }
                // Title shows main calendar + secondary calendar month
                val secondaryText = remember(displayedYear, displayedMonth, mainCalendar) {
                    when (mainCalendar) {
                        MainCalendar.SHAMSI -> {
                            val g = jdnToGregorian(jalaliToJdn(displayedYear, displayedMonth, 15))
                            "${gregorianMonthNamesEn[g.month]} ${g.year}"
                        }
                        MainCalendar.MILADI -> {
                            val j = jdnToJalali(gregorianToJdn(displayedYear, displayedMonth, 15))
                            "${jalaliMonthNames[j.month]} ${toPersianDigits(j.year)}"
                        }
                        MainCalendar.HIJRI -> {
                            val g = jdnToGregorian(hijriDateToJdn(displayedYear, displayedMonth, 15))
                            "${gregorianMonthNamesEn[g.month]} ${g.year}"
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (mainCalendar) {
                            MainCalendar.SHAMSI -> "${jalaliMonthNames[displayedMonth]} ${toPersianDigits(displayedYear)}"
                            MainCalendar.MILADI -> "${englishMonthNamesLong[displayedMonth]} ${displayedYear}"
                            MainCalendar.HIJRI -> "${hijriMonthNames[displayedMonth]} ${toPersianDigits(displayedYear)}"
                        },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = if (isShamsi || isHijri) vazirmatnFontFamily else interFontFamily
                        ),
                        color = colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = secondaryText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = if (isShamsi || isHijri) interFontFamily else vazirmatnFontFamily
                        ),
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                }
                FilledTonalIconButton(
                    onClick = { nextMonth() },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = colorScheme.surface.copy(alpha = 0.85f),
                        contentColor = colorScheme.onSurface
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = s.next, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Direction follows the calendar (RTL Shamsi/Hijri, LTR Gregorian)
            val gridDirection = if (isShamsi || isHijri) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides gridDirection) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weekdayHeaders.forEach { name ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = if (isShamsi || isHijri) vazirmatnFontFamily else interFontFamily
                                ),
                                color = colorScheme.onPrimaryContainer.copy(alpha = 0.92f),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Swipeable month grid pager
            CompositionLocalProvider(LocalLayoutDirection provides gridDirection) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    beyondViewportPageCount = 1,
                    key = { it }
                ) { page ->
                    val (pageYear, pageMonth) = remember(page, mainCalendar) {
                        when (mainCalendar) {
                            MainCalendar.SHAMSI -> pageToJalaliMonth(page, todayJalaliYear, todayJalaliMonth)
                            MainCalendar.MILADI -> pageToGregorianMonth(page, todayGregorianYear, todayGregorianMonth)
                            MainCalendar.HIJRI -> pageToHijriMonth(page, todayHijriYear, todayHijriMonth)
                        }
                    }
                    val firstJdn = when (mainCalendar) {
                        MainCalendar.SHAMSI -> jalaliToJdn(pageYear, pageMonth, 1)
                        MainCalendar.MILADI -> gregorianToJdn(pageYear, pageMonth, 1)
                        MainCalendar.HIJRI -> hijriDateToJdn(pageYear, pageMonth, 1)
                    }
                    val firstOffset = (weekdayFromJdn(firstJdn) - weekStart.startIdx + 7) % 7
                    val monthLen = when (mainCalendar) {
                        MainCalendar.SHAMSI -> jalaliMonthLength(pageYear, pageMonth)
                        MainCalendar.MILADI -> gregorianMonthLength(pageYear, pageMonth)
                        MainCalendar.HIJRI -> hijriMonthLength(pageYear, pageMonth)
                    }
                    val dayFont = if (isShamsi || isHijri) vazirmatnFontFamily else interFontFamily

                    Column(modifier = Modifier.fillMaxWidth()) {
                        for (row in 0 until 6) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (col in 0 until 7) {
                                    val dayNum = row * 7 + col - firstOffset + 1
                                    Box(
                                        modifier = Modifier.weight(1f).aspectRatio(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dayNum in 1..monthLen) {
                                            val dayJdn = when (mainCalendar) {
                                                MainCalendar.SHAMSI -> jalaliToJdn(pageYear, pageMonth, dayNum)
                                                MainCalendar.MILADI -> gregorianToJdn(pageYear, pageMonth, dayNum)
                                                MainCalendar.HIJRI -> hijriDateToJdn(pageYear, pageMonth, dayNum)
                                            }
                                            val isSelected = dayJdn == selectedJdn
                                            val isToday = dayJdn == todayJdn
                                            val dayText = if (isShamsi || isHijri) toPersianDigits(dayNum) else dayNum.toString()
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(colorScheme.surface)
                                                        .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f), CircleShape)
                                                        .clickable { onDaySelected(dayJdn) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = dayText,
                                                        color = colorScheme.onSurface,
                                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = dayFont),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clickable { onDaySelected(dayJdn) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = dayText,
                                                            color = colorScheme.onPrimaryContainer,
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = dayFont),
                                                            fontSize = 15.sp
                                                        )
                                                        if (isToday) {
                                                            Box(
                                                                Modifier.padding(top = 2.dp).size(5.dp).clip(CircleShape)
                                                                    .background(colorScheme.onPrimaryContainer.copy(alpha = 0.95f))
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// === Detail Card - surfaceContainer, selected date in three calendars ===
@Composable
private fun DetailCard(
    selectedJdn: Int,
    language: AppLanguage
) {
    val colorScheme = MaterialTheme.colorScheme
    val isEnglish = language == AppLanguage.ENGLISH
    val vazirmatnFontFamily = VazirmatnFontFamily()
    val interFontFamily = InterFontFamily()

    val selectedJalali = remember(selectedJdn) { jdnToJalali(selectedJdn) }
    val selectedGregorian = remember(selectedJdn) { jdnToGregorian(selectedJdn) }
    val selectedHijri = remember(selectedJdn) { jdnToHijri(selectedJdn) }
    val selectedWeekdayIdx = (selectedJdn + 1) % 7 // Sun 0..Sat 6

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorScheme.surfaceContainer,
            contentColor = colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEnglish) englishWeekdays[selectedWeekdayIdx]
                       else persianWeekdaysLarge[selectedWeekdayIdx],
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = if (isEnglish) interFontFamily else vazirmatnFontFamily
                ),
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            )
            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 16.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateColumn(
                        day = toPersianDigitsPadded(selectedJalali.day, 2),
                        month = jalaliMonthNames[selectedJalali.month],
                        year = toPersianDigits(selectedJalali.year),
                        font = vazirmatnFontFamily,
                        onSurface = colorScheme.onSurface
                    )
                    DateColumn(
                        day = selectedGregorian.day.toString().padStart(2, '0'),
                        month = if (isEnglish) gregorianMonthNamesEn[selectedGregorian.month] else gregorianMonthNamesFa[selectedGregorian.month],
                        year = selectedGregorian.year.toString(),
                        font = interFontFamily,
                        onSurface = colorScheme.onSurface
                    )
                    DateColumn(
                        day = toPersianDigitsPadded(selectedHijri.day, 2),
                        month = hijriMonthNames[selectedHijri.month],
                        year = toPersianDigits(selectedHijri.year),
                        font = vazirmatnFontFamily,
                        onSurface = colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// === Settings Dialog ===
@Composable
private fun SettingsDialog(
    seedIndex: Int,
    onSeedIndexChange: (Int) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    weekStart: WeekStart,
    onWeekStartChange: (WeekStart) -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    mainCalendar: MainCalendar,
    onMainCalendarChange: (MainCalendar) -> Unit,
    strings: AppStrings,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isEnglish = language == AppLanguage.ENGLISH
    val s = strings

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AppShapes.extraLarge,
        containerColor = colorScheme.surfaceContainerHigh,
        title = {
            Text(
                s.settings,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main calendar selector
                Text(s.mainCalendar, style = MaterialTheme.typography.titleSmall, color = colorScheme.onSurface)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = mainCalendar == MainCalendar.SHAMSI,
                        onClick = { onMainCalendarChange(MainCalendar.SHAMSI) },
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                        icon = {}
                    ) { Text(s.shamsi, style = MaterialTheme.typography.labelMedium) }
                    SegmentedButton(
                        selected = mainCalendar == MainCalendar.MILADI,
                        onClick = { onMainCalendarChange(MainCalendar.MILADI) },
                        shape = RoundedCornerShape(0.dp),
                        icon = {}
                    ) { Text(s.miladi, style = MaterialTheme.typography.labelMedium) }
                    SegmentedButton(
                        selected = mainCalendar == MainCalendar.HIJRI,
                        onClick = { onMainCalendarChange(MainCalendar.HIJRI) },
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                        icon = {}
                    ) { Text(s.hijri, style = MaterialTheme.typography.labelMedium) }
                }

                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Language selector
                Text(s.language, style = MaterialTheme.typography.titleSmall, color = colorScheme.onSurface)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = language == AppLanguage.PERSIAN,
                        onClick = { onLanguageChange(AppLanguage.PERSIAN) },
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                        icon = {}
                    ) { Text("دری / فارسی", style = MaterialTheme.typography.labelMedium) }
                    SegmentedButton(
                        selected = language == AppLanguage.ENGLISH,
                        onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                        icon = {}
                    ) { Text("English", style = MaterialTheme.typography.labelMedium) }
                }

                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Theme color selector - name under each circle
                Text(s.themeColor, style = MaterialTheme.typography.titleSmall, color = colorScheme.onSurface)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    seedOptions.forEachIndexed { idx, opt ->
                        val isSelected = idx == seedIndex
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(opt.color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) colorScheme.primary else colorScheme.outlineVariant,
                                        shape = CircleShape
                                    )
                                    .clickable { onSeedIndexChange(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = if (opt.color == AfghanSeeds.Yellow) Color(0xFF221900) else Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Text(
                                if (isEnglish) opt.nameEn else opt.nameFa,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Theme mode
                Text(s.appearance, style = MaterialTheme.typography.titleSmall, color = colorScheme.onSurface)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                        icon = {}
                    ) { Text(s.light, style = MaterialTheme.typography.labelMedium) }
                    SegmentedButton(
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                        shape = RoundedCornerShape(0.dp),
                        icon = {}
                    ) { Text(s.system, style = MaterialTheme.typography.labelMedium) }
                    SegmentedButton(
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { onThemeModeChange(ThemeMode.DARK) },
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                        icon = {}
                    ) { Text(s.dark, style = MaterialTheme.typography.labelMedium) }
                }

                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Week start - user selectable: Saturday / Sunday / Monday
                Text(s.weekStart, style = MaterialTheme.typography.titleSmall, color = colorScheme.onSurface)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    WeekStart.entries.forEachIndexed { idx, option ->
                        SegmentedButton(
                            selected = weekStart == option,
                            onClick = { onWeekStartChange(option) },
                            shape = when (idx) {
                                0 -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                                WeekStart.entries.lastIndex -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                                else -> RoundedCornerShape(0.dp)
                            },
                            icon = {}
                        ) {
                            Text(
                                if (isEnglish) option.labelEn else option.labelFa,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                Text(
                    s.offlineNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(s.close) }
        }
    )
}
