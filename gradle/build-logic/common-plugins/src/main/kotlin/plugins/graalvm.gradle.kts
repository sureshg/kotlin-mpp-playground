package plugins

import GithubAction
import Platform
import com.javiersc.semver.project.gradle.plugin.SemverExtension
import dev.suresh.gradle.*
import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
  application
  id("plugins.kotlin.jvm")
  id("plugins.misc")
  id("org.graalvm.buildtools.native")
  id("com.autonomousapps.dependency-analysis")
}

val debugEnabled = project.hasProperty("debug")
val quickBuildEnabled = project.hasProperty("quick")
val nativeBundleEnabled = project.hasProperty("bundle")
val muslEnabled = project.hasProperty("musl")

application {
  mainClass = libs.versions.app.mainclass
  applicationDefaultJvmArgs += buildList {
    addAll(jvmArguments)
    add("--show-version")
    add("--add-modules=$addModules")
  }
}

val semverExtn = extensions.getByType<SemverExtension>()

graalvmNative {
  binaries {
    named("main") {
      imageName = project.name
      mainClass = application.mainClass
      useFatJar = false
      sharedLibrary = false
      fallback = false
      verbose = debugEnabled
      quickBuild = quickBuildEnabled
      buildArgs = buildList {
        add("--enable-preview")
        add("--native-image-info")
        add("--enable-monitoring=heapdump,jfr,jvmstat")
        add("--enable-https")
        add("--install-exit-handlers")
        add("--features=dev.suresh.aot.RuntimeFeature")
        add("-R:MaxHeapSize=64m")
        add("-H:+ReportExceptionStackTraces")
        add("-EBUILD_NUMBER=${project.version}")
        add("-ECOMMIT_HASH=${ semverExtn.commits.get().first().hash}")
        // add("--enable-url-protocols=http,https,jar,unix")
        // add("-H:IncludeResources=.*(message\\.txt|\\app.properties)\$")

        if (Platform.isLinux) {
          when {
            muslEnabled -> {
              add("--static")
              add("--libc=musl")
            }
            else -> add("-H:+StaticExecutableWithDynamicLibC")
          }
          add("-H:+StripDebugInfo")
        }

        // Use the compatibility mode when build image on GitHub Actions.
        when (GithubAction.isEnabled) {
          true -> add("-march=compatibility")
          else -> add("-march=native")
        }

        if (debugEnabled) {
          add("-H:+TraceNativeToolUsage")
          add("-H:+TraceSecurityServices")
          add("--trace-class-initialization=kotlin.annotation.AnnotationRetention")
        }

        if (nativeBundleEnabled) {
          add("--bundle-create")
          add("--dry-run")
        }
      }
      jvmArgs = listOf("--add-modules=$addModules")
      systemProperties = mapOf("java.awt.headless" to "false")
      resources { autodetect() }
    }
  }
  metadataRepository { enabled = true }
  toolchainDetection = false
}

/**
 * Creates a custom sourceset(`graal`) for GraalVM native image build time configurations. The
 * following configurations will
 * - Creates a `graal` source set.
 * - Add `main` output to `graal` compile and runtime classpath.
 * - Add `main` dependencies to `graal` compile and runtime classpath.
 * - Add `graal` dependencies (graalImplementation) to native-image classpath.
 * - Add `graal` output to native-image classpath.
 *
 * For each source set added to the project, the Java plugins add a few
 * [dependency configurations](https://docs.gradle.org/current/userguide/java_plugin.html#java_source_set_configurations)
 * - graalImplementation
 * - graalCompileOnly
 * - graalRuntimeOnly
 * - graalCompileClasspath (CompileOnly + Implementation)
 * - graalRuntimeClasspath (RuntimeOnly + Implementation)
 *
 * [Configure-Custom-SourceSet](https://docs.gradle.org/current/userguide/java_testing.html#sec:configuring_java_integration_tests)
 */
val graal by
    sourceSets.creating {
      compileClasspath += sourceSets.main.get().output
      runtimeClasspath += sourceSets.main.get().output
    }

configurations {
  val graalImplementation by existing
  val graalRuntimeOnly by existing

  // graalImplementation extendsFrom main source set implementation
  graalImplementation.extendsFrom(implementation)
  graalRuntimeOnly.extendsFrom(runtimeOnly)

  // Finally, nativeImage classpath extendsFrom graalImplementation
  // This way all main + graal dependencies are also available at native image build time.
  nativeImageClasspath.extendsFrom(graalImplementation)
}

tasks {
  val archiveTgz by
      creating(Tar::class) {
        archiveFileName = archiveName()
        compression = Compression.GZIP
        destinationDirectory = project.layout.buildDirectory
        from(nativeCompile.map { it.outputFile })
        doLast {
          // Set the output for the GitHub native-build action.
          with(GithubAction) {
            setOutput("version", project.version)
            setOutput("native_image_name", archiveFileName.get())
            setOutput("native_image_path", archiveFile.get().asFile.absolutePath)
          }
          val binFile = archiveFile.get().asFile
          logger.lifecycle(
              "Native Image Archive: ${binFile.absolutePath} (${binFile.length().byteDisplaySize()})")
        }
      }

  nativeCompile { finalizedBy(archiveTgz) }

  dependencyAnalysis { issues { this.all { onAny { severity("warn") } } } }

  // shadowJar { mergeServiceFiles() }
}

fun archiveName() = buildString {
  append(project.name)
  append("-")
  append(project.version)
  append("-")
  if (muslEnabled) {
    append("static-")
  }
  if (nativeBundleEnabled) {
    append("bundle-")
  }
  append(Platform.currentOS.id)
  append("-")
  append(Platform.currentArch.isa)
  append(".tar.gz")
}

dependencies {
  // Dependencies required for native-image build. Use "graalCompileOnly" for compile only deps.
  // "graalCompileOnly"(libs.graalvm.sdk)
  "graalImplementation"(libs.classgraph)
  nativeImageCompileOnly(graal.output)

  // kapt(libs.graalvm.hint.processor)
  // compileOnly(libs.graalvm.hint.annotations)
}
