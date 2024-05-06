package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.PluginAware
import org.gradle.kotlin.dsl.apply

/** A sample Gradle plugin shows how to use as a [Project] and [Settings] plugin. */
class GenericPlugin : Plugin<PluginAware> {
  override fun apply(target: PluginAware) =
      when (target) {
        is Project -> target.pluginManager.apply(GenericProjectPlugin::class)
        is Settings -> target.pluginManager.apply(GenericSettingsPlugin::class)
        else -> error("GenericPlugin cannot be applied to ${target::class}")
      }
}

class GenericProjectPlugin : Plugin<Project> {
  override fun apply(target: Project) =
      with(target) {
        with(pluginManager) {
          // println(TextColors.cyan("Applied the generic project plugin for $name"))
          // apply("org.jetbrains.compose")
        }
      }
}

class GenericSettingsPlugin : Plugin<Settings> {
  override fun apply(target: Settings) =
      with(target) {
        // println("Applied the generic settings plugin for ${rootProject.name}")
        gradle.beforeProject { pluginManager.apply(GenericProjectPlugin::class) }

        // Configure an extension when a plugin is applied.
        pluginManager.withPlugin("com.gradle.develocity") {}

        // pluginManager.withPlugin("org.graalvm.buildtools.native") {
        //   val graalExt = extensions.findByType(GraalVMExtension::class.java)
        //   graalExt?.ext {}
        // }
      }
}
