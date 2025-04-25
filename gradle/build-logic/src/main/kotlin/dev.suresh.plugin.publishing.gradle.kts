import com.google.cloud.tools.jib.gradle.JibExtension
import common.*
import java.time.Year
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
  `maven-publish`
  signing
  com.gradleup.nmcp
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
      url = uri(githubPackage(libs.versions.dev.name.get(), rootProject.name))
      credentials {
        username = githubPackagesUsername.orNull
        password = githubPackagesPassword.orNull
      }
    }
  }

  publications {
    // Kotlin Multiplatform
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
      val javadocJar by
          tasks.registering(Jar::class) {
            archiveClassifier = "javadoc"
            duplicatesStrategy = DuplicatesStrategy.WARN
            // Contents are deliberately left empty
            // from(tasks.named("dokkaJavadoc"))
          }

      // KMP will automatically create the publications
      withType<MavenPublication>().configureEach {
        artifact(javadocJar)
        configurePom()
      }
    }

    // Kotlin JVM ("org.jetbrains.kotlin.jvm")
    pluginManager.withPlugin("java") {
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

    // Java Platform (BOM)
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
      val dokkaGenerateJar by
          tasks.registering(Jar::class) {
            from(tasks.named("dokkaGenerate"))
            archiveClassifier = "html-docs"
          }

      withType<MavenPublication>().configureEach { artifact(dokkaGenerateJar) }
    }
  }
}

// Configures GHCR credentials for Jib
pluginManager.withPlugin("com.google.cloud.tools.jib") {
  configure<JibExtension> {
    to {
      if (image.orEmpty().startsWith("ghcr.io", ignoreCase = true)) {
        auth {
          username = githubPackagesUsername.orNull
          password = githubPackagesPassword.orNull
        }
      }
    }
  }
}

signing {
  setRequired { hasSigningKey }
  if (hasSigningKey) {
    useInMemoryPgpKeys(
        signingInMemoryKeyId.orNull, signingInMemoryKey.orNull, signingInMemoryKeyPassword.orNull)
    sign(publishing.publications)
  }
}

nmcp {
  centralPortal {
    username = mavenCentralUsername
    password = mavenCentralPassword
  }
}

fun MavenPublication.configurePom() {
  pom {
    name = provider { "${project.group}:${project.name}" }
    description = provider { project.description }
    inceptionYear = Year.now().toString()
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

  withType<AbstractPublishToMaven>().configureEach { mustRunAfter(withType<Sign>()) }

  // For publishing kotlin native binaries
  withType<AbstractPublishToMaven>().configureEach { mustRunAfter(withType<KotlinNativeLink>()) }
}
