import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
            }
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
        }
    }
}

android {
    namespace = "app.afghancalendar"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.afghancalendar"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "1.0.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        abortOnError = false
        disable.add("NullSafeMutableLiveData")
    }
}

compose.desktop {
    application {
        mainClass = "app.afghancalendar.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi)
            packageName = "Afghan Calendar"
            packageVersion = "1.0.4"
            vendor = "Taikutsu Lyrz"
            description = "Afghan Calendar — Shamsi, Miladi & Hijri in one place. Offline-first."
            copyright = "© 2026 Taikutsu Lyrz"
            // do NOT set iconFile unless a real ico exists; omit so default icon is used
            windows {
                // WiX for Msi, Inno Setup not required for Exe when using jpackage defaults? For Compose 1.7.3, TargetFormat.Msi uses WiX, Exe produces app-image + exe launcher. Use defaults.
                // If iconFile needed, point to existing resource only if exists. Since no custom ico, omit.
                shortcut = true
                menuGroup = "Afghan Calendar"
                // perUserInstall can be false for machine-wide; leave default
            }
        }
    }
}
