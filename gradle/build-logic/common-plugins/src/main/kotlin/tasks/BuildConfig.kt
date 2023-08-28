package tasks

import com.javiersc.semver.project.gradle.plugin.SemverExtension
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.output.StringOutput
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin

@CacheableTask
abstract class BuildConfig @Inject constructor(private val extension: BuildConfigExtension) :
    DefaultTask() {

  @get:[OutputDirectory Optional]
  val generatedOutputDir: DirectoryProperty = extension.outputDir

  @get:Internal internal val templateName = "BuildConfig.kte"

  init {
    description = "Generate build config class"
    group = LifecycleBasePlugin.BUILD_TASK_NAME
  }

  @TaskAction
  fun execute() {
    val dir = generatedOutputDir.get().asFile
    dir.deleteRecursively()
    dir.mkdirs()

    val fqName = extension.classFqName.get()
    val className = fqName.substringAfterLast('.')
    val pkg = fqName.substringBeforeLast(".", "")

    val file = dir.resolve("$className.kt")
    logger.quiet("Generated build config file: ${file.absolutePath}")

    // Get git commit info
    val gitCommit = run {
      val commit = project.the<SemverExtension>().commits.get().first()
      mapOf(
          "gitHash" to commit.hash,
          "gitMessage" to commit.message,
          "gitFullMessage" to commit.fullMessage.trim(),
          "gitTimestampEpochSecond" to commit.timestampEpochSecond.toString(),
          "gitTags" to commit.tags.joinToString(),
      )
    }

    // the<VersionCatalogsExtension>().named("libs").
    val params =
        mapOf(
            "className" to className,
            "pkg" to pkg,
            "version" to extension.version.get(),
            "gitCommit" to gitCommit,
            "catalogVersions" to extension.catalogVersions.get(),
            "dependencies" to extension.dependencies.get(),
        )

    val content = StringOutput()
    val tmplEngine =
        TemplateEngine.createPrecompiled(ContentType.Plain).apply { setTrimControlStructures(true) }

    tmplEngine.render(templateName, params, content)
    file.writeText(content.toString())
  }
}

open class BuildConfigExtension(@Inject private val project: Project) {

  @get:Input val classFqName = project.objects.property<String>().convention("BuildConfig")

  @get:Input val version = project.objects.property<String>().convention(project.version.toString())

  @get:Input
  val catalogVersions = project.objects.mapProperty<String, String>().convention(emptyMap())

  @get:Input val dependencies = project.objects.listProperty<String>().convention(emptyList())

  @get:Input
  val outputDir =
      project.objects
          .directoryProperty()
          .convention(project.layout.buildDirectory.dir("generated/buildconfig"))

  //  @get:[Input Optional]
  //  val additionalFields: MapProperty<String, Any> = project.objects.mapProperty<String, Any>()
}
