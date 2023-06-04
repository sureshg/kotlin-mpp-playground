package plugins

import com.github.ajalt.mordant.rendering.TextColors
import common.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.spi.*
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.*
import tasks.*

plugins {
  idea
  java
  application
  `test-suite-base`
  id("com.github.johnrengelman.shadow")
}

if (hasCleanTask) {
  logger.warn(
      TextColors.yellow(
              """
      | CLEANING ALMOST NEVER FIXES YOUR BUILD!
      | Cleaning is often a last-ditch effort to fix perceived build problems that aren't going to
      | actually be fixed by cleaning. What cleaning will do though is make your next few builds
      | significantly slower because all the incremental compilation data has to be regenerated,
      | so you're really just making your day worse.
      """)
          .trimMargin(),
  )
}

afterEvaluate { logger.lifecycle(TextColors.magenta("=== Project Configuration Completed ===")) }

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
  }
  project.vcs = "Git"
}

// shadow plugin requires mainClass to be set
application { mainClass = "dev.suresh.MainKt" }

java {
  withSourcesJar()
  withJavadocJar()

  toolchain { configureJvmToolchain() }
}

@Suppress("UnstableApiUsage", "UNUSED_VARIABLE")
testing {
  suites {
    val test by getting(JvmTestSuite::class) { useJUnitJupiter(libs.versions.junit) }
    // OR "test"(JvmTestSuite::class) {}

    withType(JvmTestSuite::class) {
      // Configure all test suites
      targets.configureEach { testTask { configureJavaTest() } }
    }
  }
}

@Suppress("UNUSED_VARIABLE")
tasks {

  // Prints java module dependencies using jdeps
  val printModuleDeps by registering {
    description = "Print Java Platform Module dependencies of the application."
    group = LifecycleBasePlugin.BUILD_TASK_NAME

    doLast {
      val jarTask = named("shadowJar", Jar::class)
      val jarFile = jarTask.get().archiveFile.get().asFile

      val jdeps =
          ToolProvider.findFirst("jdeps").orElseGet { error("jdeps tool is missing in the JDK!") }
      val out = StringWriter()
      val pw = PrintWriter(out)
      jdeps.run(
          pw,
          pw,
          "-q",
          "-R",
          "--print-module-deps",
          "--ignore-missing-deps",
          "--multi-release=${javaRelease.get()}",
          jarFile.absolutePath,
      )

      val modules = out.toString()
      logger.quiet(
          """
          |Application modules for OpenJDK-${java.toolchain.languageVersion.get()} are,
          |${modules.split(",")
              .mapIndexed {i, module -> " ${(i+1).toString().padStart(2)}) $module" }
              .joinToString(System.lineSeparator())
             }
          """
              .trimMargin())
    }

    dependsOn("shadowJar")
  }

  val buildExecutable by
      registering(ReallyExecJar::class) {
        val shadowJar = named("shadowJar", Jar::class) // project.tasks.shadowJar
        jarFile = shadowJar.flatMap { it.archiveFile }
        // javaOpts = application.applicationDefaultJvmArgs
        javaOpts = run.get().jvmArgs
        onlyIf { OperatingSystem.current().isUnix }
      }

  // val versionCatalog = the<VersionCatalogsExtension>().named("libs")
  val copyTemplates by
      registering(Copy::class) {
        description = "Generate template classes"
        group = LifecycleBasePlugin.BUILD_TASK_NAME

        // GitHub actions workaround
        val props = project.properties.toMutableMap()
        props["git_branch"] = project.findProperty("branch_name")
        props["git_tag"] = project.findProperty("base_tag")

        // Find resolved runtime dependencies
        val dependencies =
            project.configurations
                .named("runtimeClasspath")
                .get()
                .resolvedConfiguration
                .resolvedArtifacts
                .map { it.moduleVersion.id.toString() }
                .sorted()
                .joinToString(System.lineSeparator())
        props["dependencies"] = dependencies

        // Add info from Gradle version catalog
        val versionCatalog = project.catalogs.named("libs")
        props["javaVersion"] = versionCatalog.findVersion("java").get()
        props["kotlinVersion"] = versionCatalog.findVersion("kotlin").get()
        props["gradleVersion"] = versionCatalog.findVersion("gradle").get()

        if (debugEnabled) {
          props.forEach { (t, u) -> println("%1\$-42s --> %2\$s".format(t, u)) }
        }

        filteringCharset = "UTF-8"
        from(project.projectDir.resolve("src/main/templates"))
        into(project.buildDir.resolve("generated-sources/templates/kotlin/main"))
        exclude { it.name.startsWith("jte") }
        expand(props)

        // inputs.property("buildversions", props.hashCode())
      }

  // Add the generated templates to the source set.
  sourceSets { main { java.srcDirs(copyTemplates) } }

  // jdeprscan task configuration
  val jdepExtn = extensions.create<JdeprscanExtension>("jdeprscan")
  val jdeprscan = register<Jdeprscan>("jdeprscan", jdepExtn)
  jdeprscan {
    val shadowJar by existing(Jar::class)
    jarFile = shadowJar.flatMap { it.archiveFile }
  }
}
