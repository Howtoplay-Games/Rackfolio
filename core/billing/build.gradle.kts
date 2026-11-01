plugins {
    alias(libs.plugins.portproof.android.library)
    alias(libs.plugins.portproof.hilt)
}

android {
    namespace = "com.portproof.core.billing"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:data"))
    implementation(libs.play.billing.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
}
