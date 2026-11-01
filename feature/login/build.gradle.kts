plugins {
    alias(libs.plugins.portproof.android.feature)
}

android {
    namespace = "com.portproof.feature.login"
}

dependencies {
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.activity.compose)
}
