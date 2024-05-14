package plugins

import common.*
import common.libs
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
  `maven-publish`
  signing
  com.gradleup.nmcp
  // org.cyclonedx.bom
}

// Nexus plugin needs to apply to the root project only
if (isRootProject) {
  apply(plugin = "io.github.gradle-nexus.publish-plugin")
}

group = libs.versions.group.get()

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
        // findProperty("githubActor")
        username = githubActor.orNull ?: System.getenv("GITHUB_ACTOR")
        password = githubToken.orNull ?: System.getenv("GITHUB_TOKEN")
      }
    }
  }

  publications {
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
      val javadocJar by
          tasks.registering(Jar::class) {
            archiveClassifier = "javadoc"
            duplicatesStrategy = DuplicatesStrategy.WARN
            // Contents are deliberately left empty
            // from(tasks.named("dokkaJavadoc"))
          }

      withType<MavenPublication>().configureEach {
        artifact(javadocJar)
        configurePom()
      }
    }

    // Kotlin JVM
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
      register<MavenPublication>("maven") {
        from(components["java"])
        configurePom()
      }

      // Add an executable artifact if exists
      withType<MavenPublication>().configureEach {
        // val execJar = tasks.findByName("buildExecutable") as? ReallyExecJar
        // if (execJar != null) {
        //   artifact(execJar.execJarFile)
        // }
      }
    }

    // Maven Bom
    pluginManager.withPlugin("java-platform") {
      register<MavenPublication>("maven") {
        from(components["javaPlatform"])
        configurePom()
      }
    }

    // Gradle version catalog
    pluginManager.withPlugin("version-catalog") {
      register<MavenPublication>("maven") {
        from(components["versionCatalog"])
        configurePom()
      }
    }

    // Add Dokka html doc to all publications
    pluginManager.withPlugin("org.jetbrains.dokka") {
      val dokkaHtmlJar by
          tasks.registering(Jar::class) {
            from(tasks.named("dokkaHtml"))
            archiveClassifier = "html-docs"
          }

      withType<MavenPublication>().configureEach { artifact(dokkaHtmlJar) }
    }
  }
}

nmcp {
  publishAllPublications {
    username = mavenCentralUsername
    password = mavenCentralPassword
  }
}

signing {
  setRequired { hasSigningKey }
  useInMemoryPgpKeys(signingKeyId.orNull, signingKey.orNull, signingPassword.orNull)
  sign(publishing.publications)
  // useGpgCmd()
  // isPublish = gradle.taskGraph.allTasks.any { it.name.startsWith("publish") }
}

fun MavenPublication.configurePom() {
  pom {
    name = provider { "${project.group}:${project.name}" }
    description = provider { project.description }
    inceptionYear = "2024"
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
  withType<Sign>().configureEach { onlyIf { hasSigningKey } }

  // Suppressing publication validation errors
  withType<GenerateModuleMetadata> { suppressedValidationErrors.add("enforced-platform") }

  // For publishing kotlin native binaries
  withType<PublishToMavenRepository>().configureEach { mustRunAfter(withType<KotlinNativeLink>()) }

  // cyclonedxBom {
  //   includeConfigs = listOf("runtimeClasspath")
  //   skipConfigs = listOf("compileClasspath", "testCompileClasspath")
  //   destination = project.layout.buildDirectory.dir("sbom").map { it.asFile }
  //   outputFormat = "json"
  //   includeLicenseText = true
  // }
}
