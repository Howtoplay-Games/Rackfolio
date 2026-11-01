plugins {
    alias(libs.plugins.portproof.android.library)
}

android {
    namespace = "com.portproof.core.testing"
}

dependencies {
    api(project(":core:data"))
    api(project(":core:billing"))
    api(project(":core:model"))
    api(libs.junit4)
    api(libs.kotlinx.coroutines.test)
    api(libs.kotlin.test)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui.test.junit4)
}
