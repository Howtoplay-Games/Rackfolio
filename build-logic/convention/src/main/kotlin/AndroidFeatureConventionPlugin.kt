import com.portproof.buildlogic.lib
import com.portproof.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Feature modules: Compose UI + Hilt ViewModels + type-safe navigation routes.
 * Features depend on core modules only, never on other features.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("portproof.android.library")
            pluginManager.apply("portproof.android.library.compose")
            pluginManager.apply("portproof.hilt")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                add("implementation", project(":core:model"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:ui"))
                add("implementation", libs.lib("androidx-compose-material3"))
                add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
                add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
                add("implementation", libs.lib("androidx-hilt-lifecycle-viewmodel-compose"))
                add("implementation", libs.lib("androidx-navigation-compose"))
                add("implementation", libs.lib("kotlinx-serialization-json"))

                add("testImplementation", project(":core:testing"))
                add("testImplementation", libs.lib("kotlinx-coroutines-test"))
                add("testImplementation", libs.lib("turbine"))
                add("testImplementation", libs.lib("robolectric"))
                add("testImplementation", libs.lib("androidx-test-core"))
                add("testImplementation", libs.lib("androidx-test-ext-junit"))
                add("testImplementation", libs.lib("androidx-compose-ui-test-junit4"))
                add("debugImplementation", libs.lib("androidx-compose-ui-test-manifest"))
            }
        }
    }
}
