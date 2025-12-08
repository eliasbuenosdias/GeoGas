plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.geogas"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.geogas"
        minSdk = 26
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // implementation(libs.androidx.activity.compose)
    //implementation(platform(libs.androidx.compose.bom))
    //implementation(libs.androidx.compose.ui)
    //implementation(libs.androidx.compose.ui.graphics)
    //implementation(libs.androidx.compose.ui.tooling.preview)
    //implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //androidTestImplementation(platform(libs.androidx.compose.bom))
    //androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    //debugImplementation(libs.androidx.compose.ui.tooling)
    //debugImplementation(libs.androidx.compose.ui.test.manifest)
    // osmdroid (OpenStreetMap)
    implementation(libs.osmdroid.android)

    // Retrofit + Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Room (Java)
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)

    // Lifecycle/ViewModel (por si no están)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)


    // Para manejar permisos de internet
    implementation(libs.androidx.activity)

    // Material Design Components
    implementation(libs.material)

    // AndroidX CardView
    implementation(libs.androidx.cardview)

    // AndroidX ConstraintLayout (opcional pero recomendado)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.mockito.core)
}