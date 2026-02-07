// This is the main module-level Gradle build configuration file (build.gradle.kts).
// It configures the Android application plugin, defines build settings (compileSdk, namespace, etc.),
// and declares all dependencies for the project using the version catalog from libs.versions.toml.
plugins {
    alias(libs.plugins.android.application)  // Apply the Android application plugin for building Android apps
    alias(libs.plugins.kotlin.android)       // Apply the Kotlin Android plugin for Kotlin language support
    id("kotlin-kapt")                        // Apply the Kotlin annotation processor plugin for Room
    alias(libs.plugins.dagger.hilt.android)
}

android {
    namespace = "com.example.jazzlibraryktroomjpcompose"  // Unique namespace for the application
    compileSdk = 35                                         // Compile against Android API level 34

    defaultConfig {
        applicationId = "com.example.jazzlibraryktroomjpcompose"  // Package name for the app
        minSdk = 26                                               // Minimum Android API level supported
        targetSdk = 35                                            // Target Android API level for optimizations
        versionCode = 1                                           // Internal version number for updates
        versionName = "1.0"                                       // User-visible version name

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"  // Test runner for instrumentation tests
        vectorDrawables {
            useSupportLibrary = true  // Enable vector drawable support for older APIs
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false  // Disable code shrinking for release (can be enabled for production)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),  // Default Android ProGuard rules
                "proguard-rules.pro"                                       // Custom ProGuard rules for the project
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17  // Java 17 source compatibility
        targetCompatibility = JavaVersion.VERSION_17  // Java 17 bytecode target
    }
    kotlinOptions {
        jvmTarget = "17"  // Target JVM 17 for Kotlin compilation
    }
    buildFeatures {
        compose = true     // Enable Jetpack Compose for UI
        buildConfig = true // Enable generated BuildConfig class
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"  // Kotlin compiler extension version for Compose
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"  // Exclude duplicate license files from packaging
        }
    }}

dependencies {
    implementation(libs.androidx.core.ktx)                    // Core KTX extensions for Android framework
    implementation(libs.androidx.lifecycle.runtime.ktx)       // Lifecycle runtime KTX extensions
    implementation(libs.androidx.activity.compose)            // Compose integration with Activity
    implementation(platform(libs.androidx.compose.bom))       // Compose BOM for version management
    implementation(libs.androidx.ui)                          // Compose UI foundational components
    implementation(libs.androidx.ui.graphics)                 // Compose graphics and drawing APIs
    implementation(libs.androidx.ui.tooling.preview)          // Compose preview tooling support
    implementation(libs.androidx.material3)                   // Material Design 3 components for Compose
    implementation(libs.androidx.material.icons.extended)     // Add this

    // Add these compose runtime dependencies
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.livedata)

    // Room Database
    implementation(libs.androidx.room.runtime)               // Room runtime for database operations
    implementation(libs.androidx.room.ktx)                   // Room KTX extensions for Coroutines
    kapt(libs.androidx.room.compiler)                        // Room annotation processor for code generation

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)          // Kotlin Coroutines for Android

    // ===== HILT DEPENDENCIES =====
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)  // For navigation with Hilt

    // ===== NETWORKING =====
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // ===== LIFECYCLE =====
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)  // For Compose integration
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.junit)                           // JUnit 4 for unit tests
    androidTestImplementation(libs.androidx.junit)           // AndroidX JUnit extensions for instrumented tests
    androidTestImplementation(libs.androidx.espresso.core)   // Espresso for UI tests
    androidTestImplementation(platform(libs.androidx.compose.bom))  // Compose BOM for Android tests
    androidTestImplementation(libs.androidx.ui.test.junit4)  // Compose UI testing JUnit 4 integration
    debugImplementation(libs.androidx.ui.tooling)            // Compose tooling for debug builds
    debugImplementation(libs.androidx.ui.test.manifest)      // Test manifest for Compose UI tests in debug


}

// Allow references to generated code for Hilt
kapt {
    correctErrorTypes = true
}