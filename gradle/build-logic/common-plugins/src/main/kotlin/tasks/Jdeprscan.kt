package tasks

import common.javaToolchainPath
import common.libs
import java.io.*
import javax.inject.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.model.*
import org.gradle.api.plugins.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.process.*

@CacheableTask
abstract class Jdeprscan
@Inject
constructor(
    private val layout: ProjectLayout,
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val execOps: ExecOperations,
    private val extension: JdeprscanExtension
) : DefaultTask() {

  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val jarFile: RegularFileProperty

  @get:[Input Optional]
  val classPath = objects.listProperty<File>()

  @get:Internal internal val projectName = project.name

  init {
    description = "Scans the $projectName jar file for usages of deprecated APIs"
    group = BasePlugin.BUILD_GROUP
  }

  @TaskAction
  fun execute() {
    val jdsArgs = buildList {
      if (classPath.getOrElse(emptyList()).isNotEmpty()) {
        val classPath =
            classPath.getOrElse(emptyList()).joinToString(separator = File.pathSeparator) {
              it.absolutePath
            }
        add("--class-path $classPath")
      }

      if (extension.forRemoval.get()) {
        add("--for-removal")
      }

      add("--release")
      add(extension.release.get().toString())

      if (extension.verbose.get()) {
        add("--verbose")
      }

      add(jarFile.get().asFile.absolutePath)
    }

    // java bin directory
    val jdeprscan = project.javaToolchainPath.resolve("bin").resolve("jdeprscan")
    val bos = ByteArrayOutputStream()
    val execResult =
        execOps.exec {
          workingDir = layout.buildDirectory.get().asFile
          commandLine = listOf(jdeprscan.toString())
          args = jdsArgs
          standardOutput = bos
          errorOutput = bos
        }
    execResult.assertNormalExitValue()
    val deprecations = bos.toString(Charsets.UTF_8)
    println(deprecations)

    // Another way to access the process output as a provider (lazy)
    // val output: Provider<String> = project.providers.exec {}.standardOutput.asText

    // Just documenting for future use.
    if (extension.verbose.get()) {
      // Runtime classpath to execute the source.
      val srcClassPath =
          project.extensions
              .findByType(SourceSetContainer::class.java)
              ?.named("main")
              ?.get()
              ?.runtimeClasspath
              ?.files
              ?.joinToString(separator = File.pathSeparator) { it.absolutePath }
      logger.debug("srcClassPath: $srcClassPath")

      val runtimeClasspath =
          project.configurations.named("runtimeClasspath").get().files.joinToString(
              separator = File.pathSeparator) {
                it.absolutePath
              }
      logger.debug("runtimeClasspath: $runtimeClasspath")

      val resolvedRuntimeClasspath =
          project.configurations
              .named("runtimeClasspath")
              .get()
              .resolvedConfiguration
              .resolvedArtifacts
              .joinToString(separator = File.pathSeparator) { it.file.absolutePath }
      logger.debug("resolvedRuntimeClasspath: $resolvedRuntimeClasspath")
    }
  }
}

open class JdeprscanExtension(@Inject private val project: Project) {

  @get:Input
  val release =
      project.objects.property<Int>().apply {
        val javaVersion = project.libs.versions.java.asProvider()
        set(javaVersion.map { it.toInt() })
      }

  @get:Input val forRemoval = project.objects.property<Boolean>().convention(false)

  @get:Input val verbose = project.objects.property<Boolean>().convention(false)
}
