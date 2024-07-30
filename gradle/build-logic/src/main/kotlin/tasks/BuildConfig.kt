package tasks

import com.javiersc.semver.project.gradle.plugin.Commit
import gg.jte.generated.precompiled.StaticTemplates
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin

@CacheableTask
abstract class BuildConfig @Inject constructor(@Nested val extn: BuildConfigExtension) :
    DefaultTask() {

  @get:Internal internal val templateName = "BuildConfig.kte"

  init {
    description = "Generate build config class"
    group = LifecycleBasePlugin.BUILD_GROUP
  }

  @TaskAction
  fun execute() {
    val dir = extn.outputDir.asFile.get()
    dir.deleteRecursively()
    dir.mkdirs()

    val fqName = extn.classFqName.get()
    val className = fqName.substringAfterLast('.')
    val pkg = fqName.substringBeforeLast(".", "")

    val file = dir.resolve("$className.kt")
    logger.quiet("Generated build config file: ${file.name}")

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
            "version" to extn.projectVersion.get(),
        )

    //  val content = StringOutput()
    //  val tmplEngine = TemplateEngine.createPrecompiled(ContentType.Plain).apply {
    // setTrimControlStructures(true) }
    //  tmplEngine.render(templateName, params, content)

    val content =
        StaticTemplates()
            .BuildConfig(
                className = className,
                pkg = pkg,
                projectProps = rootProjectProps,
                gitCommit = gitCommit,
                catalogVersions = extn.catalogVersions.get(),
                dependencies = extn.dependencies.get())
            .render()

    file.writeText(content)
    // outputs.dirs(extn.outputDir)
  }
}

open class BuildConfigExtension @Inject constructor(layout: ProjectLayout, objects: ObjectFactory) {
  @get:Input val classFqName = objects.property<String>().convention("BuildConfig")
  @get:Input val projectVersion = objects.property<String>()
  @get:Input val projectName = objects.property<String>()
  @get:Input val projectDesc = objects.property<String>()
  @get:Input val catalogVersions = objects.mapProperty<String, String>().convention(emptyMap())
  @get:Input val dependencies = objects.listProperty<String>().convention(emptyList())
  @Internal val gitCommit = objects.property<Commit>()
  @get:[OutputDirectory Optional]
  val outputDir =
      objects.directoryProperty().convention(layout.buildDirectory.dir("generated/buildconfig"))
}
