@file:Suppress("UnstableApiUsage")

import com.github.ajalt.mordant.rendering.TextColors
import com.google.cloud.tools.jib.api.buildplan.ImageFormat
import com.google.cloud.tools.jib.gradle.extension.nativeimage.JibNativeImageExtension
import common.githubRepo
import common.githubUser
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
  plugins.kotlin.mpp
  plugins.publishing
  com.google.cloud.tools.jib
}

val appBinName = "app"

description = "Ktor native application"

kotlin {
  targets.withType<KotlinNativeTarget>().configureEach {
    binaries {

      // Creates Release executable
      executable(setOf(RELEASE)) {
        entryPoint = "main"

        // Alpine(apk add gcompat) - https://youtrack.jetbrains.com/issue/KT-38876
        // linkerOpts("--as-needed", "--defsym=isnan=isnan")
        // freeCompilerArgs += listOf("-Xoverride-konan-properties=linkerGccFlags=-lgcc -lgcc_eh
        // -lc")

        if (buildType == NativeBuildType.RELEASE) {
          mavenPublication {
            artifact(outputFile) {
              // classifier = target.targetName
            }
          }
        }
      }

      // Creates test executable
      test(emptySet())
    }

    compilations.configureEach {
      compileTaskProvider.configure {
        compilerOptions {
          freeCompilerArgs.appendAll(
              "-Xverbose-phases=Linker"
              // "-Xruntime-logs=gc=info"
              )
        }
      }
    }
  }

  sourceSets {
    commonMain { dependencies { api(projects.shared) } }
    nativeMain {
      dependencies {
        api(libs.kmp.appdirs)
        // api(libs.arrow.suspendapp.ktor)
      }
    }
  }

  // val nativeTargetNames = targets.withType<KotlinNativeTarget>().names
}

jib {
  from {
    // Distroless is not yet supported for Kotlin Native
    // image = "gcr.io/distroless/base-debian12"
    image = "debian:stable-slim"
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

  // Use LinuxX64 for container image
  val linkReleaseExecutableLinuxX64 by getting(KotlinNativeLink::class)
  val prepareJib by
      registering(Copy::class) {
        // from(linkReleaseExecutableLinuxArm64)
        from(linkReleaseExecutableLinuxX64)
        // Jib native image extension expects the native image to be in "native/nativeCompile"
        into(layout.buildDirectory.dir("native/nativeCompile"))
        rename { appBinName }
      }

  jibDockerBuild {
    dependsOn(prepareJib)
    doLast {
      val portMapping = jib?.container?.ports.orEmpty().joinToString(" ") { "-p $it:$it" }
      val image = jib?.to?.image ?: project.name
      val tag = jib?.to?.tags?.firstOrNull() ?: "latest"
      logger.lifecycle(
          TextColors.cyan(
              """
              |Run: docker run -it --rm --name ${project.name} $portMapping $image:$tag
              """
                  .trimMargin()))
    }
  }

  // publish { finalizedBy(jibDockerBuild) }
}
