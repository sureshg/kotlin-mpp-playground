package tasks

import java.util.*
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.*
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation

private val Project.sourceSets: SourceSetContainer
  get() = extensions.getByName("sourceSets") as SourceSetContainer

private val Project.javaToolchains: JavaToolchainService
  get() = extensions.getByName("javaToolchains") as JavaToolchainService

/**
 * Setup tasks to compile `module-info.java` file for a module named [moduleName] from a source set
 * with name [sourceSetName] using Java toolchain with version [toolchainVersion].
 *
 * It is assumed that source set with [sourceSetName] only extends main JVM source set.
 * [parentCompilation] represent a compilation corresponding to such a main source set.
 *
 * For more details, check this [kotlinx-io](https://github.com/Kotlin/kotlinx-io/pull/406/files) PR
 */
fun Project.configureJava9ModuleInfoCompilation(
    sourceSetName: String,
    toolchainVersion: JavaLanguageVersion,
    parentCompilation: KotlinJvmCompilation,
    moduleName: String
) {
  val moduleOutputs = listOf(parentCompilation.output.allOutputs)
  val compileClasspathConfiguration =
      parentCompilation.configurations.compileDependencyConfiguration
  val sourceSetNameCapitalized =
      sourceSetName.replaceFirstChar { it.titlecase(Locale.getDefault()) }
  val javaCompileClasspath = configurations["${sourceSetName}CompileClasspath"]
  javaCompileClasspath.extendsFrom(compileClasspathConfiguration)

  tasks.named("compile${sourceSetNameCapitalized}Java", JavaCompile::class.java) {
    dependsOn(moduleOutputs)

    targetCompatibility = "9"
    sourceCompatibility = "9"

    javaCompiler = javaToolchains.compilerFor { languageVersion = toolchainVersion }
    val javaSourceSet = sourceSets[sourceSetName].java
    destinationDirectory =
        javaSourceSet.destinationDirectory.asFile.get().resolve("META-INF/versions/9")
    options.sourcepath = files(javaSourceSet.srcDirs)
    val moduleFiles = objects.fileCollection().from(moduleOutputs)
    val modulePath = javaCompileClasspath.filter { it !in moduleFiles.files }
    dependsOn(modulePath)
    classpath = objects.fileCollection().from()
    options.compilerArgumentProviders.add(
        JigsawArgumentsProvider(moduleName, moduleFiles, modulePath))
  }
}

private class JigsawArgumentsProvider(
    private val moduleName: String,
    private val moduleFiles: FileCollection,
    private val modulePath: FileCollection
) : CommandLineArgumentProvider {
  override fun asArguments(): Iterable<String> =
      listOf(
          "--module-path",
          modulePath.asPath,
          "--patch-module",
          "$moduleName=${moduleFiles.asPath}",
          "--release",
          "9")
}
