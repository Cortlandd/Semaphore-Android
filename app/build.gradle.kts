plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.paparazzi)

    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"

    // For ksp and ROOM
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"

    id("com.google.dagger.hilt.android") version "2.57.2"
    id("androidx.navigation.safeargs.kotlin") version "2.9.5"

}

val admobAppId = providers.gradleProperty("ADMOB_APP_ID")
    .orElse("ca-app-pub-3940256099942544~3347511713")
val admobBannerAdUnitId = providers.gradleProperty("ADMOB_BANNER_AD_UNIT_ID")
    .orElse("ca-app-pub-3940256099942544/9214589741")

android {
    namespace = "com.cortlandwalker.semaphore"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cortlandwalker.semaphore"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["admobAppId"] = admobAppId.get()
        buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"${admobBannerAdUnitId.get()}\"")
        buildConfigField("String", "REMOVE_ADS_PRODUCT_ID", "\"remove_ads\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    hilt {
        enableAggregatingTask = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    configurations.all {
        exclude(group = "xpp3", module = "xpp3")
    }
}

dependencies {

    implementation("com.github.Cortlandd:Ghettoxide:1.0.9")
    implementation("com.github.Cortlandd:klipy-android-sdk:0.1.4")

    // Markdown
    implementation("com.github.jeziellago:compose-markdown:0.5.8")

    // Material icons extended
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.google.play.services.ads)
    implementation(libs.google.play.billing)

    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.databinding.adapters)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.paparazzi)
    testImplementation("com.google.guava:guava:33.2.1-android")
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.dynamic.features.fragment)
    androidTestImplementation(libs.androidx.navigation.testing)

    // Serialization / Retrofit
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization.converter)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.fragment)

    // AppCompat
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.appcompat.resources)

    // Picker
    api("com.seo4d696b75.compose:material3-picker:0.2.0")
}

ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

configurations.all { 
    resolutionStrategy.force("com.google.guava:guava:33.2.1-android") 
}
