@file:Suppress("UnstableApiUsage")

import com.github.ajalt.mordant.rendering.TextColors.magenta
import common.byteDisplaySize

plugins {
  java
  com.gradleup.shadow
  dev.suresh.plugin.publishing
}

description = "OpenTelemetry agent with custom instrumentation!"

java {
  withSourcesJar()
  withJavadocJar()
}

val otel by configurations.registering { isTransitive = false }

// Task to repackage the OpenTelemetry agent with custom extensions
val extendedAgent by
    tasks.registering(Jar::class) {
      description = "Creates extended OpenTelemetry agent with extensions"
      group = "build"

      val otelAgentJar = zipTree(otel.map { it.singleFile })
      val extnJar = tasks.shadowJar.map { it.archiveFile }

      from(otelAgentJar)
      from(extnJar) { into("extensions") }
      archiveFileName = "otel-javaagent.jar"

      // Preserve MANIFEST.MF file from the upstream javaagent
      doFirst {
        manifest.from(otelAgentJar.matching { include("META-INF/MANIFEST.MF") }.singleFile)
      }

      doLast {
        val agent = archiveFile.get().asFile
        logger.lifecycle(
            magenta(
                "OpenTelemetry Agent: ${agent.absolutePath} (${agent.length().byteDisplaySize()})"))
      }
    }

// Replace the 'shadowJar' jar with the 'extendedAgent`
configurations {
  default {
    outgoing.artifacts.clear()
    outgoing.artifact(extendedAgent)
  }

  apiElements {
    outgoing.artifacts.clear()
    outgoing.artifact(extendedAgent)
  }

  runtimeElements {
    outgoing.artifacts.clear()
    outgoing.artifact(extendedAgent)
  }

  this.shadow {
    outgoing.artifacts.clear()
    outgoing.artifact(extendedAgent)
  }

  shadowRuntimeElements { outgoing.artifacts.clear() }
}

tasks {
  compileJava { options.release = 17 }

  test {
    useJUnitPlatform()
    inputs.files(layout.files(extendedAgent))
  }

  assemble { dependsOn(extendedAgent) }
}

dependencies {
  // Interfaces and SPIs that we implement. We use `compileOnly` dependency because
  // during runtime all necessary classes are provided by javaagent itself.
  implementation(platform(libs.otel.bom))
  implementation(platform(libs.otel.instr.bom))
  compileOnly(libs.bundles.otel.agent.extn)
  compileOnly(libs.google.auto.annotations)
  implementation(libs.otel.samplers)
  annotationProcessor(libs.google.auto.service.apt)

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.slf4j.simple)

  otel(libs.otel.instr.javaagent)
}
