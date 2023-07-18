package plugins

import common.Repo
import common.libs

plugins {
  `maven-publish`
  signing
  id("org.cyclonedx.bom")
}

// Nexus plugin needs to apply to the root project only
if (project == rootProject) {
  apply(plugin = "io.github.gradle-nexus.publish-plugin")
}

group = libs.versions.group.get()

tasks {
  cyclonedxBom {
    includeConfigs = listOf("runtimeClasspath")
    skipConfigs = listOf("compileClasspath", "testCompileClasspath")
    destination = project.layout.buildDirectory.dir("sbom").map { it.asFile }
    outputFormat = "json"
    includeLicenseText = true
  }
}

publishing {
  repositories {
    maven {
      name = "local"
      url = uri(layout.buildDirectory.dir("repo"))
    }

    maven {
      name = "GitHubPackages"
      url = uri(Repo.githubPackage(libs.versions.publish.dev.name.get(), project.name))
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
      }
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

      // 2. Add executable jar as an artifact
      if (project == rootProject) {
        withType<MavenPublication>().configureEach {
          // val buildExecutable by tasks.existing
          // artifact(buildExecutable)
          // artifact(tasks.shadowJar)
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
            archiveClassifier = "html-docs"
          }

      // For publishing a pure kotlin project
      val dokkaJavadocJar by
          tasks.registering(Jar::class) {
            from(tasks.named("dokkaJavadoc"))
            archiveClassifier = "javadoc"
            duplicatesStrategy = DuplicatesStrategy.WARN
          }

      withType<MavenPublication>().configureEach {
        // add dokka html jar as an artifact
        artifact(dokkaHtmlJar)
        artifact(dokkaJavadocJar)
      }
    }
  }
}

// signing {
//  setRequired {
//    gradle.taskGraph.allTasks.any {
//      it.name.startsWith("publish")
//    }
//  }
//  publishing.publications.configureEach {
//    sign(this)
//  }
//  useGpgCmd()
// }

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
