import common.githubUser
import common.javaVersion
import common.jvmArguments
import common.tmp

plugins {
  plugins.kotlin.jvm
  plugins.publishing
  application
  alias(libs.plugins.ktor)
  alias(libs.plugins.exposed)
  com.google.cloud.tools.jib
}

description = "Ktor backend jvm application"

application {
  mainClass = libs.versions.app.mainclass.get()
  applicationDefaultJvmArgs += jvmArguments(forAppRun = true)
}

ktor { fatJar { archiveFileName = "${project.name}-app.jar" } }

jib {
  from {
    image = "openjdk:${javaVersion.get().majorVersion}-slim"
    platforms {
      platform {
        architecture = "arm64"
        os = "linux"
      }
    }
  }

  to {
    image = "${project.githubUser}/${project.name}"
    tags = setOf("latest")
  }

  container {
    ports = listOf("8080", "9898")
    entrypoint = buildList {
      add("java")
      addAll(application.applicationDefaultJvmArgs.map { it.replace(tmp, "/tmp/") })
      add("-cp")
      add("@/app/jib-classpath-file")
      add("@/app/jib-main-class-file")
    }
    mainClass = application.mainClass.get()
  }

  containerizingMode = "packaged"
}

exposedCodeGeneratorConfig { outputDirectory.set(file("src/main/kotlin/dev/suresh")) }

// Configuration to copy webapp to resources
val webapp by configurations.creating

tasks {
  val copyWebApp by
      registering(Copy::class) {
        from(webapp)
        into(processResources.map { it.destinationDir.resolve(webapp.name) })
      }
  processResources { dependsOn(copyWebApp) }

  // publish { finalizedBy(jibDockerBuild) }
}

dependencies {
  implementation(projects.common)
  // Server dependencies
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.server.metrics.micrometer)
  implementation(libs.ktor.server.call.logging)
  implementation(libs.ktor.server.status.pages)
  implementation(libs.ktor.server.default.headers)
  implementation(libs.ktor.server.forwarded.header)
  implementation(libs.ktor.server.swagger)
  // implementation(libs.ktor.server.openapi)
  implementation(libs.ktor.server.http.redirect)
  implementation(libs.ktor.server.compression)
  implementation(libs.ktor.server.cors)
  implementation(libs.ktor.server.host.common)
  implementation(libs.ktor.server.auto.head)
  implementation(libs.ktor.server.partial.content)
  implementation(libs.ktor.server.resources)
  implementation(libs.ktor.server.auth)
  implementation(libs.ktor.server.auth.jwt)
  implementation(libs.ktor.serialization.json)
  // Client dependencies
  implementation(libs.ktor.client.java)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.client.encoding)
  implementation(libs.ktor.client.logging)
  implementation(libs.ktor.client.resources)
  implementation(libs.ktor.client.auth)
  // Database
  implementation(libs.exposed.core)
  implementation(libs.exposed.jdbc)
  implementation(libs.exposed.dao)
  implementation(libs.exposed.kotlin.datetime)
  implementation(libs.postgresql)
  implementation(libs.hikariCP)
  implementation(libs.sherlock.sql)
  // Monitoring
  implementation(libs.ktor.cohort.core)
  implementation(libs.ktor.cohort.hikari)
  implementation(libs.micrometer.prometheus)
  implementation(libs.ap.loader.all)
  // Logging
  implementation(libs.logback.classic)
  // Testing
  testImplementation(platform(libs.testcontainers.bom))
  testImplementation(libs.ktor.server.tests)
  testImplementation(libs.testcontainers.junit5)
  testImplementation(libs.testcontainers.postgresql)
  testImplementation(libs.konsist)

  // Copy web app browserDist
  webapp(project(path = ":${projects.web.name}", configuration = webapp.name))

  // Specify the classifier using variantOf
  // implementation(variantOf(libs.lwjgl.opengl) { classifier("natives-linux") })
}
