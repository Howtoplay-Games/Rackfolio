plugins {
    alias(libs.plugins.portproof.android.feature)
}

android {
    namespace = "com.portproof.feature.subscription"
}

dependencies {
    implementation(project(":core:billing"))
}
