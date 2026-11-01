import com.android.build.api.dsl.LibraryExtension
import com.portproof.buildlogic.configureKotlinAndroid
import com.portproof.buildlogic.lib
import com.portproof.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                testOptions.animationsDisabled = true
            }
            dependencies {
                add("testImplementation", libs.lib("junit4"))
                add("testImplementation", libs.lib("kotlin-test"))
            }
        }
    }
}
