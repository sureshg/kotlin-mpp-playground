package plugins

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedVariantResult
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*

/**
 * The plugin to resolve runtime dependencies and generate reports.
 *
 * [Accessing the resolution result
 * programmatically](https://docs.gradle.org/current/userguide/dependency_resolution.html#sec:programmatic_api)
 */
class DepReportsPlugin : Plugin<Project> {
  override fun apply(target: Project) =
      with(target) {
        pluginManager.withPlugin("java-base") {
          val listResolvedArtifacts by
              tasks.registering(ListResolvedArtifacts::class) {
                // Get the runtime resolved artifacts
                val runtimeClasspath by target.configurations
                val resolvedArtifacts = runtimeClasspath.incoming.artifacts.resolvedArtifacts

                // Transform the artifacts
                artifactIds = resolvedArtifacts.map { it.map(ResolvedArtifactResult::getId) }
                artifactVariants =
                    resolvedArtifacts.map { it.map(ResolvedArtifactResult::getVariant) }
                artifactFiles =
                    resolvedArtifacts.map {
                      it.map { resolvedArtifactResult ->
                        layout.projectDirectory.file(resolvedArtifactResult.file.absolutePath)
                      }
                    }
                outputFile.convention(layout.buildDirectory.file("resolved-artifacts.txt"))
              }
        }
      }
}

@CacheableTask
abstract class ListResolvedArtifacts : DefaultTask() {

  @get:Input abstract val artifactIds: ListProperty<ComponentArtifactIdentifier>

  @get:Input abstract val artifactVariants: ListProperty<ResolvedVariantResult>

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val artifactFiles: ListProperty<RegularFile>

  @get:OutputFile abstract val outputFile: RegularFileProperty

  @TaskAction
  fun execute() {
    val ids = artifactIds.get()
    val variants = artifactVariants.get()
    val files = artifactFiles.get()
    val outFile = outputFile.asFile.get()

    outFile.bufferedWriter().use {
      ids.forEachIndexed { idx, id ->
        val variant = variants[idx]
        val file = files[idx]
        it.appendLine("File - ${file.asFile.name}")
        it.appendLine("  Id : ${id.displayName}")
        it.appendLine("  Variant : ${variant.displayName}")
        it.appendLine("  Size : ${file.asFile.length()}")
        it.appendLine()
      }
    }
    outFile.forEachLine { println(it) }
  }
}
