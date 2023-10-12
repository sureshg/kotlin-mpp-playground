import common.jvmArguments

plugins {
  plugins.kotlin.jvm
  plugins.publishing
  alias(libs.plugins.ktor)
  alias(libs.plugins.exposed)
  application
}

description = "Ktor backend jvm application"

application {
  mainClass = libs.versions.app.mainclass.get()
  applicationDefaultJvmArgs += jvmArguments(forAppRun = true)
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
  // Logging
  implementation(libs.logback.classic)
  // Testing
  testImplementation(platform(libs.testcontainers.bom))
  testImplementation(libs.ktor.server.tests)
  testImplementation(libs.testcontainers.junit5)
  testImplementation(libs.testcontainers.postgresql)

  // Copy web app browserDist
  webapp(project(path = ":${projects.web.name}", configuration = webapp.name))

  // Specify the classifier using variantOf
  // implementation(variantOf(libs.lwjgl.opengl) { classifier("natives-linux") })
}
