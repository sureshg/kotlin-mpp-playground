package tasks

import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.output.StringOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.language.base.plugins.LifecycleBasePlugin

@CacheableTask
abstract class BuildConfig : DefaultTask() {

  @get:[Input Optional]
  abstract val classFqName: Property<String>

  @get:[Input Optional]
  abstract val additionalFields: MapProperty<String, Any>

  @get:[OutputDirectory Optional]
  abstract val generatedOutputDir: DirectoryProperty

  init {
    description = "Generate build config class"
    group = LifecycleBasePlugin.BUILD_TASK_NAME
    // Set the default values.
    classFqName.convention("BuildConfig")
    generatedOutputDir.convention(project.layout.buildDirectory.dir("generated/buildconfig"))
    additionalFields.convention(emptyMap())
  }

  @TaskAction
  fun execute() {
    val dir = generatedOutputDir.get().asFile
    dir.deleteRecursively()
    dir.mkdirs()

    val fqName = classFqName.get()
    val className = fqName.substringAfterLast('.')
    val pkg = fqName.substringBeforeLast(".", "")

    val file = dir.resolve("$className.kt")
    logger.quiet("Generated build config file: ${file.absolutePath}")

    // the<VersionCatalogsExtension>().named("libs").
    val params =
        mapOf("className" to className, "pkg" to pkg, "additionalFields" to additionalFields.get())

    val content = StringOutput()
    val tmplEngine =
        TemplateEngine.createPrecompiled(ContentType.Plain).apply { setTrimControlStructures(true) }

    tmplEngine.render("BuildConfig.kte", params, content)
    file.writeText(content.toString())
  }
}
