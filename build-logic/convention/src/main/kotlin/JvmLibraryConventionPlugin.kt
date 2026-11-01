import com.portproof.buildlogic.PortProofBuild
import com.portproof.buildlogic.configureKotlinJvm
import com.portproof.buildlogic.lib
import com.portproof.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Pure Kotlin/JVM modules (no Android dependencies), e.g. the domain model. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = PortProofBuild.javaVersion
                targetCompatibility = PortProofBuild.javaVersion
            }
            configureKotlinJvm()
            dependencies {
                add("testImplementation", libs.lib("junit4"))
                add("testImplementation", libs.lib("kotlin-test"))
            }
        }
    }
}
