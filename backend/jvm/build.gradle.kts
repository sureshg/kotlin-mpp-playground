import com.github.ajalt.mordant.rendering.TextColors
import com.google.cloud.tools.jib.api.buildplan.ImageFormat
import com.google.devtools.ksp.gradle.KspAATask
import common.*
import kotlin.io.path.Path

plugins {
  plugins.kotlin.jvm
  application
  alias(libs.plugins.ktor)
  alias(libs.plugins.exposed)
  com.google.cloud.tools.jib
  gg.jte.gradle
  plugins.publishing
}

description = "Ktor backend jvm application"

application {
  mainClass = libs.versions.app.mainclass.get()
  applicationDefaultJvmArgs += jvmArguments(appRun = true)
}

ktor { fatJar { archiveFileName = "${project.name}-app.jar" } }

jte {
  contentType = gg.jte.ContentType.Html
  sourceDirectory =
      sourceSets.main.map { it.resources.srcDirs.first().resolve("templates").toPath() }
  generate()
}

exposedCodeGeneratorConfig { outputDirectory.set(file("src/main/kotlin/dev/suresh")) }

jib {
  from {
    image = "openjdk:${javaVersion.get().majorVersion}-slim"

    platforms {
      platform {
        architecture = "arm64"
        os = "linux"
      }
      platform {
        architecture = "amd64"
        os = "linux"
      }
    }
  }

  to {
    image = "${project.githubUser}/${project.name}"
    tags = setOf("latest")
  }

  container {
    appRoot = "/app"
    ports = listOf("8080", "9898")
    entrypoint = buildList {
      add("java")
      add("-javaagent:${appRoot}/otel/otel-javaagent.jar")
      add("-Dotel.service.name=${project.name}")
      add("-Dotel.javaagent.logging=application")
      add("-Dnet.bytebuddy.experimental=true")
      // add("-Dotel.javaagent.enabled=false")
      // add("-Dotel.traces.exporter=logging")
      // add("-Dotel.metrics.exporter=logging")
      // add("-Dotel.logs.exporter=logging")
      // add("-Dotel.instrumentation.kotlinx-coroutines.enabled=false")
      addAll(application.applicationDefaultJvmArgs.map { it.replace(tmp, "/tmp/") })
      add("-cp")
      add("@${appRoot}/jib-classpath-file")
      add("@${appRoot}/jib-main-class-file")
    }

    environment =
        mapOf(
            "OTEL_JAVAAGENT_ENABLED" to "false",
            "OTEL_TRACES_EXPORTER" to "logging",
            "OTEL_METRICS_EXPORTER" to "logging",
            "OTEL_LOGS_EXPORTER" to "logging",
            // "OTEL_RESOURCE_ATTRIBUTES" to
            // "service.name=${project.name},service.namespace=${project.group},service.instance.id=localhost:8080",
        )

    args = listOf(project.name, project.version.toString())
    mainClass = application.mainClass.get()
    expandClasspathDependencies = true
    format = ImageFormat.OCI

    labels =
        mapOf(
            "maintainer" to project.githubUser,
            "org.opencontainers.image.authors" to project.githubUser,
            "org.opencontainers.image.title" to project.name,
            "org.opencontainers.image.description" to "🐳 ${project.description}",
            "org.opencontainers.image.version" to project.version.toString(),
            "org.opencontainers.image.vendor" to project.githubUser,
            "org.opencontainers.image.url" to project.githubRepo,
            "org.opencontainers.image.source" to project.githubRepo,
            "org.opencontainers.image.licenses" to "Apache-2.0")
  }

  containerizingMode = "packaged"

  // Copy OpenTelemetry Java agent into the container
  extraDirectories {
    paths {
      path {
        setFrom(layout.buildDirectory.dir("otel"))
        into = "${container.appRoot}/otel"
      }
    }
  }
}

// Configuration to copy JS/Wasm app to resources
val jsApp by configurations.creating
val wasmApp by configurations.creating

tasks {
  val copyTasks =
      listOf(jsApp, wasmApp).map { cnf ->
        val appResDir = Path(base = "app", cnf.name.removeSuffix("App"))
        register<Copy>("copy${cnf.name}") {
          from(cnf)
          into(processResources.map { it.destinationDir.toPath().resolve(appResDir) })
          duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
      }

  // Copy webapps to resources
  processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    dependsOn(copyTasks)
  }

  // Makes sure jte is generated before compilation
  withType<KspAATask>().configureEach { dependsOn(generateJte) }

  jibDockerBuild {
    doLast {
      val portMapping = jib?.container?.ports.orEmpty().joinToString(" ") { "-p $it:$it" }
      val image = jib?.to?.image ?: project.name
      val tag = jib?.to?.tags?.firstOrNull() ?: "latest"
      val env =
          jib?.container?.environment.orEmpty().map { "-e ${it.key}=${it.value}" }.joinToString(" ")
      logger.lifecycle(
          TextColors.cyan(
              """
              |Run: docker run -it --rm --name ${project.name} $portMapping $env $image:$tag
              """
                  .trimMargin()))
    }
  }

  // publish { finalizedBy(jibDockerBuild) }
}

dependencies {
  implementation(projects.shared)
  implementation(projects.backend.data)
  implementation(projects.backend.profiling)
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
  implementation(libs.ktor.server.websockets)
  implementation(libs.ktor.serialization.json)

  // Client dependencies
  implementation(libs.ktor.client.java)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.client.encoding)
  implementation(libs.ktor.client.logging)
  implementation(libs.ktor.client.resources)
  implementation(libs.ktor.client.auth)
  implementation(libs.ktor.client.websockets)

  // Database
  implementation(libs.exposed.core)
  implementation(libs.exposed.jdbc)
  implementation(libs.exposed.dao)
  implementation(libs.exposed.kotlin.datetime)
  implementation(libs.postgresql)
  implementation(libs.hikariCP)
  implementation(libs.sherlock.sql)

  // Templating
  implementation(libs.jte.runtime)
  // compileOnly(libs.jte.kotlin)
  implementation(libs.kotlinx.html)
  implementation(kotlinw("css"))
  implementation(libs.ktor.server.html)

  // OpenTelemetry
  javaAgent(libs.otel.instr.javaagent)
  implementation(libs.ktor.cohort.core)
  implementation(libs.ktor.cohort.hikari)
  implementation(libs.micrometer.prometheus)

  // Logging
  implementation(libs.logback.classic)

  // Testing
  testImplementation(libs.ktor.server.tests)
  testImplementation(libs.testcontainers.junit5)
  testImplementation(libs.testcontainers.postgresql)
  testImplementation(libs.testcontainers.k3s)
  testImplementation(libs.kubernetes.client)
  testImplementation(libs.konsist)

  // Copy js and wasm apps
  jsApp(project(path = projects.web.js.dependencyProject.path, configuration = "jsApp"))
  wasmApp(project(path = projects.web.wasm.dependencyProject.path, configuration = "wasmApp"))

  // Specify the classifier using variantOf
  // implementation(variantOf(libs.lwjgl.opengl) { classifier("natives-linux") })

  //  constraints {
  //    implementation(libs.kotlinx.html.get().module.toString()) {
  //      version { strictly(libs.kotlinx.html.get().version.toString()) }
  //      because("Ktor Issue!")
  //    }
  //  }
}

// Remove slf4j simple provider from tests
// configurations.testImplementation {
//    exclude(module = libs.slf4j.simple.get().module.toString())
// }

// JS build output files.
// tasks.named(jsWebpack).get().outputs.files
