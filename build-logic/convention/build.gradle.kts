import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.portproof.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        fun conventionPlugin(alias: Provider<PluginDependency>, implementation: String) {
            val pluginId = alias.get().pluginId
            register(pluginId) {
                id = pluginId
                implementationClass = implementation
            }
        }
        // asProvider(): these aliases are also prefixes of the "-compose" aliases.
        conventionPlugin(libs.plugins.portproof.android.application.asProvider(), "AndroidApplicationConventionPlugin")
        conventionPlugin(libs.plugins.portproof.android.application.compose, "AndroidApplicationComposeConventionPlugin")
        conventionPlugin(libs.plugins.portproof.android.library.asProvider(), "AndroidLibraryConventionPlugin")
        conventionPlugin(libs.plugins.portproof.android.library.compose, "AndroidLibraryComposeConventionPlugin")
        conventionPlugin(libs.plugins.portproof.android.feature, "AndroidFeatureConventionPlugin")
        conventionPlugin(libs.plugins.portproof.android.room, "AndroidRoomConventionPlugin")
        conventionPlugin(libs.plugins.portproof.hilt, "HiltConventionPlugin")
        conventionPlugin(libs.plugins.portproof.jvm.library, "JvmLibraryConventionPlugin")
    }
}
