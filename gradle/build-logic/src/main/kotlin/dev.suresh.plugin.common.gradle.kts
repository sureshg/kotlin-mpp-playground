import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.*
import com.google.cloud.tools.jib.gradle.BuildDockerTask
import me.saket.bytesize.*
import org.gradle.kotlin.dsl.*

tasks {
  withType<AbstractTestTask>().configureEach { failOnNoDiscoveredTests = false }

  pluginManager.withPlugin("com.google.cloud.tools.jib") {
    withType<BuildDockerTask>().configureEach {
      doLast {
        val indent = " ".repeat(9)
        val portMapping =
            jib?.container?.ports.orEmpty().joinToString(" \\\n $indent") { "-p $it:$it" }
        val image = jib?.to?.image ?: project.name
        val tag = jib?.to?.tags?.firstOrNull() ?: "latest"
        val env =
            jib?.container
                ?.environment
                .orEmpty()
                .map { "-e ${it.key}=${it.value}" }
                .joinToString(" \\\n $indent")

        val cmd = buildString {
          appendLine("To run the container,")
          appendLine("$ docker run \\")
          appendLine("$indent -it --rm \\")
          appendLine("$indent --name ${project.name} \\")
          if (portMapping.isNotBlank()) {
            appendLine("$indent $portMapping \\")
          }
          if (env.isNotBlank()) {
            appendLine("$indent $env \\")
          }
          appendLine("$indent $image:$tag")
        }
        logger.lifecycle(TextColors.cyan(cmd))
      }
    }
  }

  register("printArtifacts") {
    doLast {
      val configsWithArtifacts =
          project.configurations
              .filter { it.isCanBeResolved || it.isCanBeConsumed }
              .filter { it.outgoing.artifacts.isNotEmpty() }

      if (configsWithArtifacts.isEmpty()) {
        logger.lifecycle(yellow("No configurations with outgoing artifacts found"))
        return@doLast
      }

      logger.lifecycle(magenta("\n📦 Outgoing Artifacts Report"))
      logger.lifecycle("═".repeat(28))

      configsWithArtifacts.forEach { config ->
        logger.lifecycle("")
        logger.lifecycle("📎 Configuration: ${green(config.name)}")
        logger.lifecycle("├─ Consumable: ${config.isCanBeConsumed}")
        logger.lifecycle("├─ Resolvable: ${config.isCanBeResolved}")
        logger.lifecycle("└─ Artifacts:")

        config.outgoing.artifacts.forEachIndexed { index, artifact ->
          val isLast = index == config.outgoing.artifacts.size - 1
          val prefix = if (isLast) "   └─" else "   ├─"
          logger.lifecycle(
              "$prefix ${cyan(artifact.file.name)} (${artifact.file.length().decimalBytes})")
        }
      }
    }
  }

  register("dependencySizes") {
    doLast {
      val configurationName =
          project.providers.gradleProperty("configuration").getOrElse("runtimeClasspath")
      val configuration = project.configurations.named(configurationName)

      val directDeps =
          configuration
              .map { it.incoming.resolutionResult.root.dependencies }
              .get()
              .filterIsInstance<ResolvedDependencyResult>()
      logger.lifecycle("Found ${directDeps.size} direct dependencies")

      val allResolvedArtifacts =
          configuration
              .flatMap { it.incoming.artifacts.resolvedArtifacts }
              .get()
              .filterIsInstance<ResolvedArtifactResult>()
      logger.lifecycle("Found ${allResolvedArtifacts.size} resolved artifacts")
    }
  }

  //  register<Copy>("copyTemplates") {
  //    description = "Generate template classes"
  //    group = LifecycleBasePlugin.BUILD_GROUP
  //
  //    // val props = project.properties.toMutableMap()
  //    val props = mutableMapOf<String, Any?>()
  //    props["git_branch"] = project.findProperty("branch_name")
  //    props["git_tag"] = project.findProperty("base_tag")
  //
  //    // Add info from the Gradle version catalog
  //    val versionCatalog = project.catalogs.named("libs")
  //    props["javaVersion"] = versionCatalog.findVersion("java").get()
  //    props["kotlinVersion"] = versionCatalog.findVersion("kotlin").get()
  //    props["gradleVersion"] = versionCatalog.findVersion("gradle").get()
  //
  //    if (debugEnabled) {
  //      props.forEach { (t, u) -> println("%1\$-42s --> %2\$s".format(t, u)) }
  //    }
  //
  //    filteringCharset = Charsets.UTF_8.name()
  //    from(project.projectDir.resolve("src/main/templates"))
  //    into(project.layout.buildDirectory.dir("generated-sources/templates/kotlin/main"))
  //    exclude { it.name.startsWith("jte") }
  //    expand(props)
  //
  //    // inputs.property("buildversions", props.hashCode())
  //  }

  // val versionCatalog = the<VersionCatalogsExtension>().named("libs")

  // Add the generated templates to the source set to enable the task.
  // sourceSets { main { java.srcDirs(copyTemplates) } }
}
