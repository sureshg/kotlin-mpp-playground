package plugins

import com.github.ajalt.mordant.rendering.TextColors.*
import common.*
import common.Platform
import java.io.PrintWriter
import java.io.StringWriter
import java.util.spi.*
import org.gradle.api.publish.plugins.PublishingPlugin.*
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.*
import tasks.*

plugins {
  idea
  application
  com.github.johnrengelman.shadow
  id("plugins.kotlin.docs")
  id("plugins.publishing")
}

if (hasCleanTask) {
  logger.warn(
      yellow(
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

afterEvaluate {}

gradle.projectsEvaluated { logger.lifecycle(magenta("=== Projects Configuration Completed ===")) }

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
  }
  project.vcs = "Git"
}

// shadow plugin requires mainClass to be set
application { mainClass = libs.versions.app.mainclass }

@Suppress("UNUSED_VARIABLE")
tasks {
  run.invoke { args(true) }

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
                     .mapIndexed { i, module -> " ${(i + 1).toString().padStart(2)}) $module" }
                     .joinToString(System.lineSeparator())}
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

        // val props = project.properties.toMutableMap()
        val props = mutableMapOf<String, Any?>()
        props["git_branch"] = project.findProperty("branch_name")
        props["git_tag"] = project.findProperty("base_tag")

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
        into(project.layout.buildDirectory.dir("generated-sources/templates/kotlin/main"))
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

  val githubActionOutput by registering {
    description = "Set Github workflow action output for this build"
    group = LifecycleBasePlugin.BUILD_TASK_NAME

    val name = project.name
    val group = project.group
    val version = project.version
    doLast {
      with(GithubAction) {
        setOutput("name", name)
        setOutput("group", group)
        setOutput("version", version)
        setOutput("artifact_name", "$name-$version")
      }
    }
  }

  // Auto format all source files
  processResources { dependsOn(":spotlessApply") }

  // Set GitHub workflow action output for this build
  build { finalizedBy(githubActionOutput) }

  val buildAndPublish by registering {
    dependsOn(allprojects.map { it.tasks.build })
    // Starting with column(:) means root project tasks
    dependsOn(":dokkaHtmlMultiModule", ":koverHtmlReport")

    when {
      // Publishing to all repos on GitHub Action tag build
      GithubAction.isTagBuild && Platform.isLinux -> {
        logger.lifecycle(magenta("Publishing to all repositories is enabled!"))
        allprojects
            .mapNotNull { it.tasks.findByName(PUBLISH_LIFECYCLE_TASK_NAME) }
            .forEach { dependsOn(it) }
      }

      // Publish is disabled on GitHub Action PR build
      GithubAction.isEnabled -> logger.lifecycle(red("Publishing is disabled!"))

      // Publishing to local repo on other platforms
      else -> {
        logger.lifecycle(yellow("Publishing to local repo is enabled!"))
        allprojects
            .mapNotNull { it.tasks.findByName("publishAllPublicationsToLocalRepository") }
            .forEach { dependsOn(it) }
      }
    }
  }

  // Task to print the project version
  register("v") {
    description = "Print the ${rootProject.name} version!"
    doLast { println(rootProject.version.toString()) }
  }
}
