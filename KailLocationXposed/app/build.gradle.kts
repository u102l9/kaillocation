plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kail.locationxposed"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kail.locationxposed"
        minSdk = 29
        targetSdk = 36
        versionCode = 46
        versionName = "1.7.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 内置自签发布密钥：本地与 CI 无需额外配置即可产出已签名 APK。
    // 口令可用 -PKAIL_STORE_PASSWORD=xxx 覆盖，也可改 gradle.properties。
    signingConfigs {
        create("kailRelease") {
            storeFile = file("kail-release.jks")
            storePassword = (project.findProperty("KAIL_STORE_PASSWORD") as String?) ?: "kail2026"
            keyAlias = (project.findProperty("KAIL_KEY_ALIAS") as String?) ?: "kail"
            keyPassword = (project.findProperty("KAIL_KEY_PASSWORD") as String?) ?: "kail2026"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("kailRelease")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName("kailRelease")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    
    compileOnly("de.robv.android.xposed:api:82")
    
    // KSP dependencies
    val room_version = "2.7.0"
    ksp("androidx.room:room-compiler:$room_version")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}