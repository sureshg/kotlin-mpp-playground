import com.github.ajalt.mordant.rendering.TextColors
import com.google.cloud.tools.jib.gradle.extension.nativeimage.JibNativeImageExtension
import common.Platform
import common.githubUser
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
  plugins.kotlin.mpp
  plugins.publishing
  com.google.cloud.tools.jib
}

description = "Ktor native application"

kotlin {
  // configureHostTarget()

  targets.filterIsInstance<KotlinNativeTarget>().forEach {
    it.binaries { executable(listOf(RELEASE)) { entryPoint = "main" } }
    it.compilations.configureEach {
      compilerOptions.configure { freeCompilerArgs.add("-Xallocator=custom") }
    }
  }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain { dependencies { api(projects.common) } }
    // nativeMain { dependencies { api(libs.arrow.suspendapp.ktor) } }
  }
}

tasks {
  val linkReleaseExecutableLinuxX64 by getting(KotlinNativeLink::class)
  val linkReleaseExecutableMacosX64 by getting(KotlinNativeLink::class)
  val linkReleaseExecutableMacosArm64 by getting(KotlinNativeLink::class)

  val macOsUniversalBinary by
      creating(Exec::class) {
        val binName = "${project.name}-macos"
        dependsOn(linkReleaseExecutableMacosX64, linkReleaseExecutableMacosArm64)
        commandLine(
            "lipo",
            "-create",
            "-output",
            binName,
            linkReleaseExecutableMacosX64.binary.outputFile,
            linkReleaseExecutableMacosArm64.binary.outputFile)
        workingDir = layout.buildDirectory.asFile.get()
        group = "Build"
        description = "Builds universal macOS binary"

        doLast {
          logger.lifecycle(
              TextColors.cyan(
                  "Universal macOS binary created: ${layout.buildDirectory.file(binName).get().asFile.path}"))
        }
      }

  val prepareJib by
      registering(Copy::class) {
        from(linkReleaseExecutableLinuxX64)
        // Jib native image extension expects the native image to be in "native/nativeCompile"
        into(layout.buildDirectory.dir("native/nativeCompile"))
        rename { "app" }
      }

  jibDockerBuild { dependsOn(prepareJib) }
}

jib {
  from {
    image = "debian:stable-slim"
    // image = "gcr.io/distroless/java-base-debian12"
    platforms {
      platform {
        architecture = "amd64"
        os = "linux"
      }
    }
  }

  to {
    image = "${project.githubUser}/${project.name}"
    tags = setOf("latest")
  }

  pluginExtensions {
    pluginExtension {
      implementation = JibNativeImageExtension::class.qualifiedName
      properties = mapOf("imageName" to "app")
    }
  }
  container { mainClass = "MainKt" }
}

// Workaround for Jib
sourceSets.maybeCreate("main")

/** Configure Kotlin Native host target for the current OS. */
fun KotlinMultiplatformExtension.configureHostTarget() {
  // Remove all non-native targets
  // targets.filter { it.platformType !in listOf(common, native) }.forEach { targets.remove(it) }

  // Create target for the host platform.
  val hostTarget =
      when {
        Platform.isMac -> macosX64()
        Platform.isLinux -> linuxX64()
        Platform.isWin -> mingwX64()
        else ->
            throw GradleException(
                "Host OS '${Platform.currentOS}' is not supported in Kotlin/Native $project.")
      }

  hostTarget.apply {
    binaries {
      executable {
        entryPoint = "main"
        runTask?.args()
      }
    }
  }
}
