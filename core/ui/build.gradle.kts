plugins {
    alias(libs.plugins.portproof.android.library)
    alias(libs.plugins.portproof.android.library.compose)
}

android {
    namespace = "com.portproof.core.ui"
}

dependencies {
    api(project(":core:designsystem"))
    api(project(":core:model"))
    implementation(libs.androidx.compose.material3)
}
