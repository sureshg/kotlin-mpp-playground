package common

import org.gradle.api.Named
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.process.CommandLineArgumentProvider

/**
 * [Configure-With-JavaModules](https://kotlinlang.org/docs/gradle-configure-project.html#configure-with-java-modules-jpms-enabled)
 */
internal class PatchModuleArgProvider(
    @get:Input val moduleName: Provider<String>,
    @InputFiles @PathSensitive(PathSensitivity.RELATIVE) val kotlinClasses: Provider<Directory>,
) : CommandLineArgumentProvider, Named {

  @Internal override fun getName() = "PatchModuleArgProvider"

  override fun asArguments() =
      listOf("--patch-module", "${moduleName.get()}=${kotlinClasses.get().asFile.absolutePath}")
}
