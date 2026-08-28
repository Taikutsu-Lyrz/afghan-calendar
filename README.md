<div align="center">

# 🗓️ Afghan Calendar

**A beautiful, offline-first calendar app for Afghanistan — Shamsi, Miladi & Hijri in one place.**

Fast • Offline • Dari / English • Material 3

</div>

---

## ✨ Features

- **📅 Three calendars, one app** — Shamsi (Jalali), Miladi (Gregorian) and Hijri (Qamari) side by side. Pick any of them as your main calendar and swipe through its months.
- **👆 Swipeable months** — flick left or right to change months, tap any day to see it in all three calendars at once.
- **🎨 5 Afghan theme colors** — Yellow, Blue, Red, Green and Brown with Light / Dark / System appearance.
- **🌐 Dari & English** — switch the whole interface between دری/فارسی and English.
- **🗓️ Flexible week start** — start the week on Saturday (Afghan convention), Sunday or Monday.
- **⚡ Works fully offline** — no internet, no accounts, no tracking. All date math happens on your device.
- **📱 Modern Material 3 design** — smooth, expressive, and responsive on any screen size.

## 📸 Screenshots

| Light — Shamsi | Light — Miladi (Blue) | Dark — Miladi (Blue) |
|:---:|:---:|:---:|
| ![Shamsi light](.github/screenshots/main-light.png) | ![Miladi blue light](.github/screenshots/main-gregorian.png) | ![Miladi blue dark](.github/screenshots/main-dark.png) |

| Settings |
|:---:|
| ![Settings](.github/screenshots/settings.png) |

## 🚀 Getting Started

### Install on your phone

1. Download the latest APK from [Releases](../../releases) (or build it yourself — see below)
2. Open the file on your Android phone and allow "Install from unknown sources" if asked
3. Done — the calendar works immediately, no setup needed

### Build from source

Requirements: **JDK 17+** and (for Android) the **Android SDK**.

```bash
# Clone
git clone https://github.com/Taikutsu-Lyrz/afghan-calendar.git
cd afghan-calendar

# Android APK  →  composeApp/build/outputs/apk/debug/composeApp-debug.apk
./gradlew :composeApp:assembleDebug

# Or run the desktop version on your PC
./gradlew :composeApp:run
```

Install on a connected device via ADB:

```bash
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

## 🗓️ About the calendars

- **Shamsi (Jalali)** — the Solar Hijri calendar used in Afghanistan and Iran, with Dari month names (حمل، ثور، جوزا، … حوت). Implemented with the Birashk 2820-year cycle for accuracy across centuries.
- **Miladi (Gregorian)** — the international calendar, shown with English month names and digits.
- **Hijri (Qamari)** — the lunar Islamic calendar (محرم … ذوالحجة) using the tabular civil algorithm.

All conversions are verified — for example `۶ سنبله ۱۴۰۵ = Friday, August 28, 2026 = ۱۶ ربیع‌الاول ۱۴۴۸`.

## 🛠️ Built with

- [Kotlin](https://kotlinlang.org/) & [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) — one codebase for Android & Desktop
- [Material 3](https://m3.material.io/) — modern theming with dynamic light/dark palettes
- [Vazirmatn](https://github.com/rastikerdar/vazirmatn) & [Inter](https://fonts.google.com/specimen/Inter) fonts for crisp Dari and English text

## 🤝 Contributing

Found a bug or want a feature? Issues and pull requests are welcome!

## 📄 License

This project is open source — see the repo for details.

<div align="center">

**Made with ❤️ for Afghanistan**

⭐ Star this repo if you find it useful!

</div>
