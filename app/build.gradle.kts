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
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // QR код сканер
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:barcodescanner:4.4.0")
    
    // Coroutines для API
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    
    // OkHttp для HTTP запросов
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}