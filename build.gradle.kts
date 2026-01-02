// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    //alias(libs.plugins.android.application) apply false
    //alias(libs.plugins.kotlin.android) apply false
    //alias(libs.plugins.kotlin.compose) apply false
}

android {
    dependenciesInfo {
        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles (for Google Play)
        includeInBundle = false
    }
}
