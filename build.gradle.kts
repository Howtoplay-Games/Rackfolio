// Top-level build file. Module configuration lives in the convention plugins
// under build-logic/convention; declaring the plugins here (apply false) puts a
// single, consistent version of each on the build classpath.
//
// Declaring the Kotlin plugins here also upgrades the Kotlin Gradle plugin used by
// AGP 9's built-in Kotlin support (AGP otherwise defaults to KGP 2.2.10).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
}
