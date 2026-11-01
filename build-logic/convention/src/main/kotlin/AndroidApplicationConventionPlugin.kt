import com.android.build.api.dsl.ApplicationExtension
import com.portproof.buildlogic.PortProofBuild
import com.portproof.buildlogic.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = PortProofBuild.TARGET_SDK
                testOptions.animationsDisabled = true
            }
        }
    }
}
