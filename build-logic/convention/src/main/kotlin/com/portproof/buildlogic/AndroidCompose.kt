package com.portproof.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/** Enables Jetpack Compose for an Android module and adds the shared Compose dependencies. */
internal fun Project.configureAndroidCompose(extension: CommonExtension) {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    extension.buildFeatures.compose = true

    dependencies {
        val bom = libs.lib("androidx-compose-bom")
        add("implementation", platform(bom))
        add("testImplementation", platform(bom))
        add("androidTestImplementation", platform(bom))
        add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))
        add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        stabilityConfigurationFiles.add(
            rootProject.layout.projectDirectory.file("compose_compiler_config.conf"),
        )
        // Pass -Pportproof.composeReports=true to write Compose compiler metrics and reports.
        if (providers.gradleProperty("portproof.composeReports").orNull == "true") {
            metricsDestination.set(layout.buildDirectory.dir("compose-metrics"))
            reportsDestination.set(layout.buildDirectory.dir("compose-reports"))
        }
    }
}
