@file:Suppress("UnstableApiUsage")

import com.github.ajalt.mordant.rendering.TextColors
import com.google.cloud.tools.jib.api.buildplan.ImageFormat
import com.google.cloud.tools.jib.gradle.extension.nativeimage.JibNativeImageExtension
import common.*
import common.Platform
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.konan.target.Family

plugins {
  id("dev.suresh.plugin.kotlin.mpp")
  id("dev.suresh.plugin.publishing")
  id("com.google.cloud.tools.jib")
}

val appBinName = "app"

description = "Ktor native application"

kotlin {
  nativeTargets(project)

  targets.withType<KotlinNativeTarget>().configureEach {
    binaries {
      executable(setOf(RELEASE)) {
        entryPoint = "main"
        // Fix for libcrypt.so.1 not-found error on distroless
        if (target.konanTarget.family == Family.LINUX) {
          linkerOpts("-Wl,--as-needed", "-Wl,-Bstatic", "-lz", "-Wl,-Bdynamic")
          // freeCompilerArgs +=
          //     listOf(
          //         "-Xverbose-phases=Linker",
          //         "-Xruntime-logs=gc=info",
          //         "-Xoverride-konan-properties=linkerGccFlags.linux=-lgcc -lgcc_eh -lc")
        }

        if (buildType == NativeBuildType.RELEASE) {
          mavenPublication {
            artifact(outputFile) {
              // classifier = target.targetName
            }
          }
        }
      }

      test(setOf()) {}

      // sharedLib {  }
    }
  }

  sourceSets {
    commonMain { dependencies { api(projects.shared) } }
    nativeMain {
      dependencies {
        api(libs.kaml)
        api(libs.kmp.appdirs)
        api(libs.kfswatch)
        api(libs.chasm)
        api(libs.kmp.process)
      }
    }

    nativeTest {
      dependencies {
        // api(libs.ktor.server.test.host)
      }
    }
  }

  // val nativeTargetNames = targets.withType<KotlinNativeTarget>().names
}

jib {
  from {
    image = "gcr.io/distroless/cc-debian12"
    platforms {
      platform {
        architecture = "arm64"
        os = "linux"
      }
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
      properties = mapOf("imageName" to appBinName)
    }
  }

  container {
    ports = listOf("8080", "9898")
    args = listOf(project.name, project.version.toString())
    format = ImageFormat.OCI
    labels = project.containerLabels
    mainClass = "MainKt"
  }
}

// Workaround for Jib
sourceSets.maybeCreate("main")

tasks {
  val buildType = "Release"
  val macOsUniversalBinary by
      registering(Exec::class) {
        val macosX64 = named<KotlinNativeLink>("link${buildType}ExecutableMacosX64")
        val macosArm64 = named<KotlinNativeLink>("link${buildType}ExecutableMacosArm64")
        val binName = "${project.name}-macos"
        dependsOn(macosX64, macosArm64)
        commandLine(
            "lipo",
            "-create",
            "-output",
            binName,
            macosArm64.get().outputFile.get(),
            macosX64.get().outputFile.get(),
        )
        workingDir = layout.buildDirectory.dir("bin").get().asFile
        group = "Build"
        description = "Builds universal macOS binary"

        doLast {
          logger.lifecycle(
              TextColors.cyan(
                  "Universal macOS binary created: ${workingDir.resolve(binName).absolutePath}"
              )
          )
        }

        onlyIf { Platform.isMac }
      }

  val prepareJib by
      registering(Copy::class) {
        // DefaultNativePlatform.getCurrentArchitecture()
        val releaseExecutable =
            when {
              Platform.isAmd64 -> named("link${buildType}ExecutableLinuxX64")
              else -> named("link${buildType}ExecutableLinuxArm64")
            }

        from(releaseExecutable)
        // Jib native image extension expects the native image to be in "native/nativeCompile"
        into(layout.buildDirectory.dir("native/nativeCompile"))
        rename { appBinName }
      }

  jibDockerBuild { dependsOn(prepareJib) }
  // publish { finalizedBy(jibDockerBuild) }

  // Pass arguments to run task : runReleaseExecutableMacosArm64 -PrunArgs="blah blah"
  withType<Exec>().configureEach {
    val arguments = providers.gradleProperty("runArgs")
    argumentProviders.add(
        CommandLineArgumentProvider { arguments.orNull?.split(" ") ?: emptyList() }
    )
  }
}
