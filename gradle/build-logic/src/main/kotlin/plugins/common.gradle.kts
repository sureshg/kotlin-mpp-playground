package plugins

import common.*
import org.gradle.kotlin.dsl.*

plugins { application }

@Suppress("UNUSED_VARIABLE")
tasks {
  run.invoke { args(true) }

  // val versionCatalog = the<VersionCatalogsExtension>().named("libs")
  val copyTemplates by
      registering(Copy::class) {
        description = "Generate template classes"
        group = LifecycleBasePlugin.BUILD_GROUP

        // val props = project.properties.toMutableMap()
        val props = mutableMapOf<String, Any?>()
        props["git_branch"] = project.findProperty("branch_name")
        props["git_tag"] = project.findProperty("base_tag")

        // Add info from the Gradle version catalog
        val versionCatalog = project.catalogs.named("libs")
        props["javaVersion"] = versionCatalog.findVersion("java").get()
        props["kotlinVersion"] = versionCatalog.findVersion("kotlin").get()
        props["gradleVersion"] = versionCatalog.findVersion("gradle").get()

        if (debugEnabled) {
          props.forEach { (t, u) -> println("%1\$-42s --> %2\$s".format(t, u)) }
        }

        filteringCharset = Charsets.UTF_8.name()
        from(project.projectDir.resolve("src/main/templates"))
        into(project.layout.buildDirectory.dir("generated-sources/templates/kotlin/main"))
        exclude { it.name.startsWith("jte") }
        expand(props)

        // inputs.property("buildversions", props.hashCode())
      }

  // Add the generated templates to the source set to enable the task.
  // sourceSets { main { java.srcDirs(copyTemplates) } }
}
