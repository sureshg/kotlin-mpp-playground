package tasks

import com.github.ajalt.mordant.rendering.TextColors
import common.*
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.*
import net.e175.klaus.zip.ZipPrefixer
import org.gradle.api.*
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.language.base.plugins.*

/**
 * The stub script is copied from
 * [Java Stack Trace Grouper](https://github.com/keith-turner/JSG/blob/master/src/main/scripts/stub.sh)
 * project.
 */
abstract class ReallyExecJar : DefaultTask() {

  @get:InputFile abstract val jarFile: RegularFileProperty

  @get:Input abstract val javaOpts: ListProperty<String>

  @get:[OutputFile Optional]
  abstract val execJarFile: RegularFileProperty

  init {
    description = "Build executable binary"
    group = LifecycleBasePlugin.BUILD_GROUP
    javaOpts.convention(emptyList())
    execJarFile.convention(project.layout.buildDirectory.file(project.name))
  }

  @TaskAction
  fun execute() {
    // Replace the tmp path with env variable for portability.
    val shellStub =
        javaClass
            .getResourceAsStream("/exec-jar-stub.sh")
            ?.readBytes()
            ?.decodeToString()
            ?.replace(
                oldValue = """"${'$'}JAVA_OPTS"""", newValue = javaOpts.get().joinToString(" "))
            ?.replace(tmp, "${'$'}TMPDIR/")
            ?: throw GradleException("Can't find executable shell stub!")
    logger.debug("Exec jar shell stub: $shellStub")

    // Make a copy of jar file
    val binFile =
        Path(jarFile.get().asFile.path).copyTo(execJarFile.get().asFile.toPath(), overwrite = true)

    // Add shell preamble and validate the executable jar
    ZipPrefixer.applyPrefixBytesToZip(binFile, shellStub.encodeToByteArray())
    ZipPrefixer.validateZipOffsets(binFile)
    binFile.setPosixFilePermissions(PosixFilePermissions.fromString("rwxr-xr-x"))
    logger.quiet(
        TextColors.magenta(
            "Executable Binary: ${binFile.pathString} ${binFile.fileSize().byteDisplaySize()}"))
  }
}
