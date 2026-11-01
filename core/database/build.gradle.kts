plugins {
    alias(libs.plugins.portproof.android.library)
    alias(libs.plugins.portproof.android.room)
    alias(libs.plugins.portproof.hilt)
}

android {
    namespace = "com.portproof.core.database"
}

dependencies {
    api(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
}
