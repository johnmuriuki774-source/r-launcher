plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.moonanime.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.moonanime.app"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    val composeBom =
        platform("androidx.compose:compose-bom:2026.06.00")

    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.17.0")

    implementation(
        "androidx.activity:activity-compose:1.12.0"
    )

    implementation("androidx.compose.ui:ui")

    implementation(
        "androidx.compose.ui:ui-tooling-preview"
    )

    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-compose:2.9.2"
    )

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2"
    )

    implementation(
        "com.squareup.okhttp3:okhttp:5.1.0"
    )

    implementation(
        "io.coil-kt.coil3:coil-compose:3.3.0"
    )

    implementation(
        "io.coil-kt.coil-network-okhttp:3.3.0"
    )

    // Room database
    implementation(
        "androidx.room:room-runtime:2.7.2"
    )

    implementation(
        "androidx.room:room-ktx:2.7.2"
    )

    ksp(
        "androidx.room:room-compiler:2.7.2"
    )

    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )
}