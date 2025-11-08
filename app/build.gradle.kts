plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"

    // For ksp and ROOM
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"

    id("com.google.dagger.hilt.android") version "2.57.2"
    id("androidx.navigation.safeargs.kotlin") version "2.9.5"

}

android {
    namespace = "com.cortlandwalker.semaphore"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cortlandwalker.semaphore"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }

    configurations.all {
        exclude(group = "xpp3", module = "xpp3")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.databinding.adapters)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation("androidx.navigation:navigation-fragment:2.9.5")
    //implementation("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.5")
    implementation("androidx.navigation:navigation-ui:2.9.5")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.9.5")
    implementation(libs.androidx.preference)
    androidTestImplementation("androidx.navigation:navigation-testing:2.9.5")

    // Lifecycle / VM
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation(libs.androidx.fragment.ktx)

    // ✅ Kotlinx Serialization converter for Retrofit
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-android-compiler:2.57.2")
    implementation("androidx.hilt:hilt-navigation-fragment:1.3.0")

    // appcompat
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.appcompat:appcompat-resources:1.7.0")

    // Giphy
    implementation("com.giphy.sdk:ui:2.3.18")
}

ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}