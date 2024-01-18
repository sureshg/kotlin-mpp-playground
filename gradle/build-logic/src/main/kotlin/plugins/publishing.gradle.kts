package plugins

import common.Repo
import common.githubRepo
import common.libs

plugins {
  `maven-publish`
  signing
  // org.cyclonedx.bom
}

// Nexus plugin needs to apply to the root project only
if (project == rootProject) {
  apply(plugin = "io.github.gradle-nexus.publish-plugin")
}

group = libs.versions.group.get()

val sonatypeUsername: String? by project
val sonatypePassword: String? by project
val signingKey: String? by project
val signingPassword: String? by project

signing {
  isRequired = sonatypeUsername.isNullOrBlank().not() && sonatypePassword.isNullOrBlank().not()
  // isPublish = gradle.taskGraph.allTasks.any { it.name.startsWith("publish") }
  useInMemoryPgpKeys(signingKey, signingPassword)
  // useGpgCmd()
  sign(publishing.publications)
}

publishing {
  repositories {
    maven {
      name = "local"
      url = uri(layout.buildDirectory.dir("repo"))
    }

    maven {
      name = "GitHubPackages"
      url = uri(Repo.githubPackage(libs.versions.dev.name.get(), rootProject.name))
      credentials {
        username = findProperty("gpr.user") as String? ?: Repo.GITHUB_USER
        password = findProperty("gpr.key") as String? ?: Repo.GITHUB_TOKEN
      }
    }
  }

  publications {

    // Kotlin Multiplatform
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
      val javadocJar by
          tasks.registering(Jar::class) {
            archiveClassifier = "javadoc"
            duplicatesStrategy = DuplicatesStrategy.WARN
            // contents are deliberately left empty
          }

      withType<MavenPublication>().configureEach {
        artifact(javadocJar)
        configurePom()
      }
    }

    // Kotlin JVM
    plugins.withId("org.jetbrains.kotlin.jvm") {
      register<MavenPublication>("maven") {
        from(components["java"])
        configurePom()
      }

      // 2. Add executable jar as an artifact
      if (project == rootProject) {
        withType<MavenPublication>().configureEach {
          // artifact(tasks.buildExecutable)
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

    // Add Dokka html doc to all publications
    plugins.withId("org.jetbrains.dokka") {
      val dokkaHtmlJar by
          tasks.registering(Jar::class) {
            from(tasks.named("dokkaHtml"))
            archiveClassifier = "html-docs"
          }

      withType<MavenPublication>().configureEach { artifact(dokkaHtmlJar) }
    }
  }
}

fun MavenPublication.configurePom() {
  pom {
    name = provider { "${project.group}:${project.name}" }
    description = provider { project.description }
    inceptionYear = "2023"
    url = githubRepo

    developers {
      developer {
        name = libs.versions.dev.name
        email = libs.versions.dev.email
        organization = libs.versions.org.name
        organizationUrl = libs.versions.org.url
      }
    }

    licenses {
      license {
        name = "The Apache Software License, Version 2.0"
        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    }

    scm {
      url = githubRepo
      connection = "scm:git:$githubRepo.git"
      developerConnection = "scm:git:$githubRepo.git"
    }
  }
}

tasks {

  // Suppressing publication validation errors
  withType<GenerateModuleMetadata> { suppressedValidationErrors.add("enforced-platform") }

  // cyclonedxBom {
  //   includeConfigs = listOf("runtimeClasspath")
  //   skipConfigs = listOf("compileClasspath", "testCompileClasspath")
  //   destination = project.layout.buildDirectory.dir("sbom").map { it.asFile }
  //   outputFormat = "json"
  //   includeLicenseText = true
  // }
}
