@file:Suppress("UnstableApiUsage")

import com.github.ajalt.mordant.rendering.TextColors
import com.google.cloud.tools.jib.api.buildplan.ImageFormat
import com.google.cloud.tools.jib.gradle.extension.nativeimage.JibNativeImageExtension
import common.Platform
import common.githubRepo
import common.githubUser
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
  plugins.kotlin.mpp
  com.google.cloud.tools.jib
  plugins.publishing
}

val appBinName = "app"

description = "Ktor native application"

kotlin {
  targets.withType<KotlinNativeTarget>().configureEach {
    binaries {
      executable(setOf(RELEASE)) {
        entryPoint = "main"

        // Fix for libcrypt.so.1 not-found error on distroless
        if (target.targetName.startsWith("linux")) {
          linkerOpts("--as-needed")
          freeCompilerArgs += "-Xoverride-konan-properties=linkerGccFlags.linux=-lgcc -lgcc_eh -lc"
        }

        if (buildType == NativeBuildType.RELEASE) {
          mavenPublication {
            artifact(outputFile) {
              // classifier = target.targetName
            }
          }
        }
      }
    }
  }

  sourceSets {
    commonMain { dependencies { api(projects.shared) } }
    nativeMain {
      dependencies {
        api(libs.ktor.client.cio)
        api(libs.kmp.appdirs)
        api(libs.kfswatch)
        // api(libs.arrow.suspendapp.ktor)
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
    labels =
        mapOf(
            "maintainer" to project.githubUser,
            "org.opencontainers.image.authors" to project.githubUser,
            "org.opencontainers.image.title" to project.name,
            "org.opencontainers.image.description" to "🐳 ${project.description}",
            "org.opencontainers.image.version" to project.version.toString(),
            "org.opencontainers.image.vendor" to project.githubUser,
            "org.opencontainers.image.url" to project.githubRepo,
            "org.opencontainers.image.source" to project.githubRepo,
            "org.opencontainers.image.licenses" to "Apache-2.0")
    mainClass = "MainKt"
  }
}

// Workaround for Jib
sourceSets.maybeCreate("main")

tasks {
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
            linkReleaseExecutableMacosX64.outputFile.get(),
            linkReleaseExecutableMacosArm64.outputFile.get())
        workingDir = layout.buildDirectory.dir("bin").get().asFile
        group = "Build"
        description = "Builds universal macOS binary"

        doLast {
          logger.lifecycle(
              TextColors.cyan(
                  "Universal macOS binary created: ${workingDir.resolve(binName).absolutePath}"))
        }

        onlyIf { OperatingSystem.current().isMacOsX }
      }

  val prepareJib by
      registering(Copy::class) {
        // DefaultNativePlatform.getCurrentArchitecture()
        val releaseExecutable =
            when {
              Platform.isAmd64 -> named("linkReleaseExecutableLinuxX64")
              else -> named("linkReleaseExecutableLinuxArm64")
            }

        from(releaseExecutable)
        // Jib native image extension expects the native image to be in "native/nativeCompile"
        into(layout.buildDirectory.dir("native/nativeCompile"))
        rename { appBinName }
      }

  jibDockerBuild { dependsOn(prepareJib) }
  // publish { finalizedBy(jibDockerBuild) }
}
