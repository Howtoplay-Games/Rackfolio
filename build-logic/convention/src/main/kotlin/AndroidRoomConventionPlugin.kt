import androidx.room.gradle.RoomExtension
import com.portproof.buildlogic.lib
import com.portproof.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("androidx.room")
            pluginManager.apply("com.google.devtools.ksp")

            extensions.configure<RoomExtension> {
                // Exported schemas are committed: they document every released schema
                // version and are required to verify migrations.
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                add("implementation", libs.lib("androidx-room-runtime"))
                add("ksp", libs.lib("androidx-room-compiler"))
                add("testImplementation", libs.lib("androidx-room-testing"))
            }
        }
    }
}
