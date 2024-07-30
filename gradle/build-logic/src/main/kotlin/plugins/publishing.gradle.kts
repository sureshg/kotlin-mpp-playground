package plugins

import com.google.cloud.tools.jib.gradle.JibExtension
import common.*
import nmcp.NmcpPublishTask
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

  val nativeBuild: String? by project
  val composeBuild: String? by project
  val springBoot: String? by project

  nmcp {
    publishAggregation {
      project(":shared")
      project(":dep-mgmt:bom")
      project(":dep-mgmt:catalog")
      project(":meta:ksp:processor")
      project(":meta:compiler:plugin")
      project(":backend:jvm")
      project(":backend:data")
      project(":backend:profiling")
      project(":backend:security")
      project(":web")
      if (nativeBuild.toBoolean()) {
        project(":backend:native")
      }
      if (springBoot.toBoolean()) {
        project(":backend:boot")
      }
      if (composeBuild.toBoolean()) {
        project(":compose:cmp")
        // project(":compose:html")
      }

      username = mavenCentralUsername
      password = mavenCentralPassword
      publicationType = "AUTOMATIC"
    }
  }
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

// Configures GHCR credentials for Jib
pluginManager.withPlugin("com.google.cloud.tools.jib") {
  configure<JibExtension> {
    to {
      if (image.orEmpty().startsWith("ghcr.io", ignoreCase = true)) {
        auth {
          username = githubActor.orNull ?: System.getenv("GITHUB_ACTOR")
          password = githubToken.orNull ?: System.getenv("GITHUB_TOKEN")
        }
      }
    }
  }
}

signing {
  setRequired { hasSigningKey }
  if (hasSigningKey) {
    useInMemoryPgpKeys(signingKeyId.orNull, signingKey.orNull, signingPassword.orNull)
  }
  sign(publishing.publications)
  // gradle.taskGraph.allTasks.any { it.name.startsWith("publish") }
}

nmcp {
  publishAllPublications {
    username = mavenCentralUsername
    password = mavenCentralPassword
  }
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
  // Suppressing publication validation errors
  withType<GenerateModuleMetadata> { suppressedValidationErrors.add("enforced-platform") }

  // For maven central portal publications - https://github.com/gradle/gradle/issues/26091
  withType<NmcpPublishTask>().configureEach { mustRunAfter(withType<Sign>()) }

  withType<AbstractPublishToMaven>().configureEach { mustRunAfter(withType<Sign>()) }

  // For publishing kotlin native binaries
  withType<AbstractPublishToMaven>().configureEach { mustRunAfter(withType<KotlinNativeLink>()) }

  // cyclonedxBom {
  //   includeConfigs = listOf("runtimeClasspath")
  //   skipConfigs = listOf("compileClasspath", "testCompileClasspath")
  //   destination = project.layout.buildDirectory.dir("sbom").map { it.asFile }
  //   outputFormat = "json"
  //   includeLicenseText = true
  // }
}
