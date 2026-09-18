plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pumbanet.client"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pumbanet.client"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // Настройки для выпуска
        vectorDrawables.useSupportLibrary = true
    }

    // Настройки подписи релизной версии
    signingConfigs {
        create("release") {
            // Для отладки используем debug keystore
            // Для продакшена замените на ваш release keystore
            storeFile = file("../keystore/release-key.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "changeit"
            keyAlias = System.getenv("KEY_ALIAS") ?: "pumbanet"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "changeit"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Разрешить сборку нескольких APK
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true // Создать универсальный APK
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // RecyclerView, CardView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // QR код сканер
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:barcodescanner:4.4.0")

    // Coroutines для API
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // OkHttp для HTTP запросов
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON
    implementation("org.json:json:20240303")

    // Biometric
    implementation("androidx.biometric:biometric:1.1.0")
}
