package app.afghancalendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Pager window: 24000 pages, current month centered
private const val PAGER_PAGE_COUNT = 24000
private const val PAGER_INITIAL_PAGE = 12000

// Maps a pager page index to (Jalali year, month), relative to the given start month
private fun pageToJalaliMonth(page: Int, startYear: Int, startMonth: Int): Pair<Int, Int> {
    val monthsFromStart = page - PAGER_INITIAL_PAGE
    var totalMonths = startYear * 12 + (startMonth - 1) + monthsFromStart
    // Jalali year 0 does not exist in this algorithm; clamp to >= 1
    if (totalMonths < 12) totalMonths = 12
    val year = totalMonths / 12
    val month = totalMonths % 12 + 1
    return year to month
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    // Theme state - persisted via rememberSaveable
    var seedIndex by rememberSaveable { mutableStateOf(0) } // Afghan Yellow default
    var themeMode by rememberSaveable { mutableStateOf(ThemeMode.SYSTEM) }
    var weekStartSaturday by rememberSaveable { mutableStateOf(true) } // Afghan convention: Saturday
    var showSettings by remember { mutableStateOf(false) }

    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemDark
    }
    val colorScheme = remember(seedIndex, isDark) { getColorScheme(seedIndex, isDark) }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography(),
        shapes = AppShapes
    ) {
        val vazirmatnFontFamily = VazirmatnFontFamily()
        val interFontFamily = InterFontFamily()
        val todayGregorian = remember { getTodayGregorian() }
        val todayJalali = remember { gregorianToJalali(todayGregorian) }

        var selectedDate by remember { mutableStateOf(todayJalali) }

        val selectedGregorian = remember(selectedDate) { jalaliToGregorian(selectedDate) }
        val selectedHijri = remember(selectedGregorian) { gregorianToHijri(selectedGregorian) }
        val selectedWeekday = remember(selectedDate) { weekdayFromJalali(selectedDate) }

        // Swipeable month pager: each page is one Jalali month
        val pagerState = rememberPagerState(initialPage = PAGER_INITIAL_PAGE) { PAGER_PAGE_COUNT }
        val scope = rememberCoroutineScope()
        fun pageMonth(page: Int): Pair<Int, Int> = pageToJalaliMonth(page, todayJalali.year, todayJalali.month)
        val (displayedYear, displayedMonth) = remember(pagerState.currentPage) { pageMonth(pagerState.currentPage) }

        fun prevMonth() { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
        fun nextMonth() { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }

        Scaffold(
            containerColor = colorScheme.surface,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Afghan Calendar",
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = interFontFamily),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 4.dp).size(26.dp)
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
                    // === Month Card - primaryContainer tonal ===
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
                            // Month navigator with FilledTonalIconButton
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
                                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous", modifier = Modifier.size(24.dp))
                                }
                                Text(
                                    text = "${jalaliMonthNames[displayedMonth]} ${toPersianDigits(displayedYear)}",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = vazirmatnFontFamily),
                                    color = colorScheme.onPrimaryContainer,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                                )
                                FilledTonalIconButton(
                                    onClick = { nextMonth() },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = colorScheme.surface.copy(alpha = 0.85f),
                                        contentColor = colorScheme.onSurface
                                    ),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next", modifier = Modifier.size(24.dp))
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Choose weekday headers based on setting
                            val weekdayHeaders = if (weekStartSaturday)
                                arrayOf("شنبه","یکشنبه","دوشنبه","سه‌شنبه","چهارشنبه","پنجشنبه","جمعه")
                            else
                                weekdayHeaderSundayStart

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (name in weekdayHeaders) {
                                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = vazirmatnFontFamily),
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
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    beyondViewportPageCount = 1,
                                    key = { it }
                                ) { page ->
                                    val (pageYear, pageMonth) = remember(page) { pageMonth(page) }
                                    val firstJdn = jalaliToJdn(pageYear, pageMonth, 1)
                                    val firstWeekday = weekdayFromJdn(firstJdn) // Sun 0
                                    val weekStartIdx = if (weekStartSaturday) 6 else 0
                                    val firstOffset = (firstWeekday - weekStartIdx + 7) % 7
                                    val monthLen = jalaliMonthLength(pageYear, pageMonth)
                                    val todayJdn = gregorianToJdn(todayGregorian.year, todayGregorian.month, todayGregorian.day)

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        for (row in 0 until 6) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                for (col in 0 until 7) {
                                                    val pos = row * 7 + col
                                                    val dayNum = pos - firstOffset + 1
                                                    Box(
                                                        modifier = Modifier.weight(1f).aspectRatio(1f),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (dayNum in 1..monthLen) {
                                                            val isSelected = selectedDate.year == pageYear && selectedDate.month == pageMonth && selectedDate.day == dayNum
                                                            val jdn = jalaliToJdn(pageYear, pageMonth, dayNum)
                                                            val isToday = jdn == todayJdn
                                                            if (isSelected) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(40.dp)
                                                                        .clip(CircleShape)
                                                                        .background(colorScheme.surface)
                                                                        .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f), CircleShape)
                                                                        .clickable { selectedDate = JalaliDate(pageYear, pageMonth, dayNum) },
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = toPersianDigits(dayNum),
                                                                        color = colorScheme.onSurface,
                                                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = vazirmatnFontFamily),
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 16.sp
                                                                    )
                                                                }
                                                            } else {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(40.dp)
                                                                        .clip(CircleShape)
                                                                        .clickable { selectedDate = JalaliDate(pageYear, pageMonth, dayNum) },
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                        Text(
                                                                            text = toPersianDigits(dayNum),
                                                                            color = colorScheme.onPrimaryContainer,
                                                                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = vazirmatnFontFamily),
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

                    // === Detail Card - surfaceContainer ===
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
                                text = persianWeekdaysLarge[selectedWeekday],
                                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = vazirmatnFontFamily),
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
                                    // Solar (Jalali) date - first column
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = toPersianDigitsPadded(selectedDate.day, 2),
                                            color = colorScheme.onSurface,
                                            fontFamily = vazirmatnFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = jalaliMonthNames[selectedDate.month],
                                            color = colorScheme.onSurface,
                                            fontFamily = vazirmatnFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = toPersianDigits(selectedDate.year),
                                            color = colorScheme.onSurface,
                                            fontFamily = vazirmatnFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 13.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                    // Gregorian date - middle column (Latin numerals, Inter)
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = selectedGregorian.day.toString().padStart(2, '0'),
                                            color = colorScheme.onSurface,
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = gregorianMonthNamesEn[selectedGregorian.month],
                                            color = colorScheme.onSurface,
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = selectedGregorian.year.toString(),
                                            color = colorScheme.onSurface,
                                            fontFamily = interFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 13.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                    // Hijri date - last column
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = toPersianDigitsPadded(selectedHijri.day, 2),
                                            color = colorScheme.onSurface,
                                            fontFamily = vazirmatnFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = hijriMonthNames[selectedHijri.month],
                                            color = colorScheme.onSurface,
                                            fontFamily = vazirmatnFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = toPersianDigits(selectedHijri.year),
                                            color = colorScheme.onSurface,
                                            fontFamily = vazirmatnFontFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 13.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
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

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                shape = AppShapes.extraLarge,
                containerColor = colorScheme.surfaceContainerHigh,
                title = {
                    Text(
                        "تنظیمات / Settings",
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
                        // Theme color selector
                        Text("رنگ پوسته", style = MaterialTheme.typography.titleSmall, color = colorScheme.onSurface)
                        Text("Seed color - 5 Afghan palettes", style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                        ) {
                            seedOptions.forEachIndexed { idx, opt ->
                                val isSelected = idx == seedIndex
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(opt.color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) colorScheme.primary else colorScheme.outlineVariant,
                                            shape = CircleShape
                                        )
                                        .clickable { seedIndex = idx },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = if (opt.color == AfghanSeeds.Yellow) Color(0xFF221900) else Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            seedOptions.forEach { opt ->
                                Text(
                                    opt.nameFa,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Theme mode
                        Text("حالت نمایش", style = MaterialTheme.typography.titleSmall, color = colorScheme.onSurface)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = themeMode == ThemeMode.LIGHT,
                                onClick = { themeMode = ThemeMode.LIGHT },
                                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                                icon = {}
                            ) { Text("روشن", style = MaterialTheme.typography.labelMedium) }
                            SegmentedButton(
                                selected = themeMode == ThemeMode.SYSTEM,
                                onClick = { themeMode = ThemeMode.SYSTEM },
                                shape = RoundedCornerShape(0.dp),
                                icon = {}
                            ) { Text("سیستم", style = MaterialTheme.typography.labelMedium) }
                            SegmentedButton(
                                selected = themeMode == ThemeMode.DARK,
                                onClick = { themeMode = ThemeMode.DARK },
                                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                                icon = {}
                            ) { Text("تاریک", style = MaterialTheme.typography.labelMedium) }
                        }

                        HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Week start toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("شروع هفته", style = MaterialTheme.typography.titleSmall, color = colorScheme.onSurface)
                                Text(
                                    if (weekStartSaturday) "شنبه (افغانستان)" else "یکشنبه (تصویر مرجع)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = weekStartSaturday,
                                onCheckedChange = { weekStartSaturday = it }
                            )
                        }
                        Text(
                            "تقویم محلی — بدون اینترنت کار می‌کند. هفته افغان از شنبه آغاز می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettings = false }) { Text("بستن") }
                }
            )
        }
    }
}
