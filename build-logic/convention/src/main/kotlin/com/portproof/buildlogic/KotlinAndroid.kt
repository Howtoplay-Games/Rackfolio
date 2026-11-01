package com.portproof.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/** Single source of truth for SDK levels and JVM targets across all modules. */
internal object PortProofBuild {
    const val COMPILE_SDK = 37
    const val MIN_SDK = 26
    const val TARGET_SDK = 36
    val javaVersion = JavaVersion.VERSION_17
    val jvmTarget = JvmTarget.JVM_17
}

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String) = findLibrary(alias).orElseThrow {
    IllegalArgumentException("Library alias '$alias' is missing from gradle/libs.versions.toml")
}

/** Shared Android configuration for application and library modules (AGP 9 built-in Kotlin). */
internal fun Project.configureKotlinAndroid(extension: CommonExtension) {
    extension.compileSdk = PortProofBuild.COMPILE_SDK
    extension.defaultConfig.minSdk = PortProofBuild.MIN_SDK
    extension.compileOptions.sourceCompatibility = PortProofBuild.javaVersion
    extension.compileOptions.targetCompatibility = PortProofBuild.javaVersion
    // Robolectric tests need merged Android resources (strings, fonts, themes).
    extension.testOptions.unitTests.isIncludeAndroidResources = true
    extension.lint.abortOnError = true
    extension.lint.checkDependencies = false
    configureKotlinJvm()
    tasks.withType<Test>().configureEach {
        // Robolectric needs reflective access to JDK internals on JDK 17+.
        jvmArgs(
            "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
            "--add-opens=java.base/java.io=ALL-UNNAMED",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED",
        )
        maxHeapSize = "2g"
    }
}

/** Kotlin compiler settings shared by Android and plain JVM modules. */
internal fun Project.configureKotlinJvm() {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(PortProofBuild.jvmTarget)
        }
    }
}
