package tasks

import com.github.ajalt.mordant.rendering.TextColors
import com.javiersc.semver.project.gradle.plugin.Commit
import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.output.StringOutput
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin

@CacheableTask
abstract class BuildConfig @Inject constructor(private val extn: BuildConfigExtension) :
    DefaultTask() {

  @get:Input val version = extn.projectVersion

  @get:Internal internal val templateName = "BuildConfig.kte"

  @get:[OutputDirectory Optional]
  val generatedOutputDir: DirectoryProperty = extn.outputDir

  init {
    description = "Generate build config class"
    group = LifecycleBasePlugin.BUILD_TASK_NAME
  }

  @TaskAction
  fun execute() {
    val dir = generatedOutputDir.asFile.get()
    dir.deleteRecursively()
    dir.mkdirs()

    val fqName = extn.classFqName.get()
    val className = fqName.substringAfterLast('.')
    val pkg = fqName.substringBeforeLast(".", "")

    val file = dir.resolve("$className.kt")
    logger.quiet(TextColors.yellow("Generated build config file: ${file.path}"))

    // Get git commit info
    val gitCommit = run {
      val commit = extn.gitCommit.get()
      mapOf(
          "gitHash" to commit.hash,
          "gitMessage" to commit.message,
          "gitFullMessage" to commit.fullMessage,
          "gitTimestampEpochSecond" to commit.timestampEpochSecond.toString(),
          "gitTags" to commit.tags.joinToString(),
      )
    }

    // Root project properties
    val rootProjectProps =
        mapOf(
            "name" to extn.projectName.get(),
            "description" to extn.projectDesc.get(),
            "version" to version.get(),
        )

    // the<VersionCatalogsExtension>().named("libs").
    val params =
        mapOf(
            "className" to className,
            "pkg" to pkg,
            "projectProps" to rootProjectProps,
            "gitCommit" to gitCommit,
            "catalogVersions" to extn.catalogVersions.get(),
            "dependencies" to extn.dependencies.get(),
        )

    val content = StringOutput()
    val tmplEngine =
        TemplateEngine.createPrecompiled(ContentType.Plain).apply { setTrimControlStructures(true) }

    tmplEngine.render(templateName, params, content)
    file.writeText(content.toString())
    // outputs.dirs(generatedOutputDir)
  }
}

open class BuildConfigExtension @Inject constructor(layout: ProjectLayout, objects: ObjectFactory) {
  @get:Input val classFqName = objects.property<String>().convention("BuildConfig")
  @get:Input val projectName = objects.property<String>()
  @get:Input val projectDesc = objects.property<String>()
  @get:Input val gitCommit = objects.property<Commit>()
  @get:Input val catalogVersions = objects.mapProperty<String, String>().convention(emptyMap())
  @get:Input val dependencies = objects.listProperty<String>().convention(emptyList())
  val projectVersion = objects.property<String>()
  val outputDir =
      objects.directoryProperty().convention(layout.buildDirectory.dir("generated/buildconfig"))
}
