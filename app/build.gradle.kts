plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.myapplicationv2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplicationv2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "ELEVENLABS_API_KEY", "\"${property("ELEVENLABS_API_KEY")}\"")
            buildConfigField("String", "ELEVENLABS_API_KEY_FEMME", "\"${project.property("ELEVENLABS_API_KEY_FEMME")}\"")
            buildConfigField("String", "ELEVENLABS_API_KEY_HOMME", "\"${project.property("ELEVENLABS_API_KEY_HOMME")}\"")
        }
        getByName("release") {
            buildConfigField("String", "ELEVENLABS_API_KEY", "\"${property("ELEVENLABS_API_KEY")}\"")
            buildConfigField("String", "ELEVENLABS_API_KEY_FEMME", "\"${project.property("ELEVENLABS_API_KEY_FEMME")}\"")
            buildConfigField("String", "ELEVENLABS_API_KEY_HOMME", "\"${project.property("ELEVENLABS_API_KEY_HOMME")}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.okhttp)
    implementation(libs.ffmpegkitfull)
    implementation(libs.google.auth.library.oauth2.http)
    implementation(libs.firebase.analytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}