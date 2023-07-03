package plugins

import common.libs

plugins {
  `maven-publish`
  signing
}

// Nexus plugin needs to apply to the root project only
if (project == rootProject) {
  apply(plugin = "io.github.gradle-nexus.publish-plugin")
}

group = libs.versions.group.get()

publishing {
  repositories {
    maven {
      name = "local"
      url = uri(layout.buildDirectory.dir("repo"))
    }
  }

  publications {
    plugins.withId("java") {
      // 1. Maven and GitHub Package Registry publications
      listOf("maven", "gpr").forEach { name ->
        register<MavenPublication>(name) {
          from(components["java"])
          configurePom()
        }
      }
    }

    // Maven Bom
    plugins.withId("java-platform") {
      register<MavenPublication>("maven") {
        from(components["javaPlatform"])
        configurePom()
      }
    }

    // Gradle version catalog
    plugins.withId("version-catalog") {
      register<MavenPublication>("maven") {
        from(components["versionCatalog"])
        configurePom()
      }
    }

    @Suppress("UNUSED_VARIABLE")
    plugins.withId("org.jetbrains.dokka") {
      // Dokka html doc
      val dokkaHtmlJar by
          tasks.registering(Jar::class) {
            from(tasks.named("dokkaHtml"))
            archiveClassifier = "htmldoc"
          }

      // For publishing a pure kotlin project
      val emptyJar by
          tasks.registering(Jar::class) {
            archiveClassifier = "javadoc"
            archiveAppendix = "empty"
            duplicatesStrategy = DuplicatesStrategy.WARN
          }

      withType<MavenPublication>().configureEach {
        // add dokka html jar as an artifact
        artifact(dokkaHtmlJar)
      }
    }
  }
}

fun MavenPublication.configurePom() {
  val githubUrl = libs.versions.publish.scm.url
  pom {
    name = provider { "${project.group}:${project.name}" }
    description = provider { project.description }
    inceptionYear = "2023"
    url = githubUrl

    developers {
      developer {
        name = libs.versions.publish.dev.name
        email = libs.versions.publish.dev.email
        organization = libs.versions.publish.org.name
        organizationUrl = libs.versions.publish.org.url
      }
    }

    licenses {
      license {
        name = "The Apache Software License, Version 2.0"
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    }

    scm {
      url = githubUrl
      connection = githubUrl.map { "scm:git:$it.git" }
      developerConnection = githubUrl.map { "scm:git:$it.git" }
    }
  }
}
