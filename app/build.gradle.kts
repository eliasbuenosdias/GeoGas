import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}


android {
    namespace = "com.eliasbuenosdias.geogas"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.eliasbuenosdias.geogas"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.1"



        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    val storeFilePathStr = System.getenv("KEYSTORE_FILE_PATH") ?: keystoreProperties["storeFile"] as String? ?: "../geogas-release.jks"
    val storeFilePath = file(storeFilePathStr)

    signingConfigs {
        create("release") {
            if (storeFilePath.exists()) {
                storeFile = storeFilePath
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: keystoreProperties["storePassword"] as String? ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: keystoreProperties["keyAlias"] as String? ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: keystoreProperties["keyPassword"] as String? ?: ""
            }
        }
    }

    buildTypes {
        release {
            if (storeFilePath.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    implementation(libs.androidx.core.splashscreen)
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
