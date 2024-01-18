package tasks

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*

/**
 * A sample [JavaExec] task that executes class from runtime classpath is as follows:
 * ```kotlin
 * val execTask by registering(JavaExec::class) {
 *         doFirst { ... }
 *         group = "build"
 *         mainClass = "app.Main"
 *         classpath = sourceSets.main.get().runtimeClasspath
 *         args = listOf(....)
 *         outputs.dir(outDir)
 *         doLast { }
 *         finalizedBy(copyTask)
 * }
 * ```
 */
@CacheableTask
abstract class ExecTask : JavaExec() {

  @get:InputFile
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val sourceFile: RegularFileProperty

  @get:OutputDirectory abstract val outputDir: DirectoryProperty

  init {
    group = "build"
    mainClass = "app.Main"
    argumentProviders.add { listOf(outputDir.get().asFile.path, sourceFile.get().asFile.path) }
  }
}
