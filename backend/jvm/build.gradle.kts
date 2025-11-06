import com.google.cloud.tools.jib.api.buildplan.ImageFormat
import com.google.devtools.ksp.gradle.KspAATask
import common.*
import io.ktor.plugin.OpenApiPreview
import kotlin.io.path.Path

plugins {
  id("dev.suresh.plugin.kotlin.jvm")
  application
  id("com.google.cloud.tools.jib")
  id("gg.jte.gradle")
  id("dev.suresh.plugin.graalvm")
  id("com.gradleup.shadow")
  alias(libs.plugins.jetbrains.ktor)
  alias(libs.plugins.exoquery)
  id("dev.suresh.plugin.publishing")
  // alias(libs.plugins.jetbrains.exposed)
}

description = "Ktor backend jvm application"

application {
  mainClass = libs.versions.app.mainclass.get()
  // applicationDefaultJvmArgs += listOf("")
}

ktor {
  @OptIn(OpenApiPreview::class)
  openApi {
    title = rootProject.name
    version = project.version.toString()
    summary = project.description
    description = project.description
    target = project.layout.buildDirectory.file("open-api.json")
  }

  fatJar { archiveFileName = "${project.name}-all.jar" }
}

jte {
  contentType = gg.jte.ContentType.Html
  sourceDirectory =
      sourceSets.main.map { it.resources.srcDirs.first().resolve("templates").toPath() }
  generate()
  jteExtension("gg.jte.models.generator.ModelExtension") { property("language", "Kotlin") }
}

// exposedCodeGeneratorConfig { outputDirectory.set(file("src/main/kotlin/dev/suresh")) }

jib {
  from {
    image = "openjdk:${javaVersion.get().majorVersion}-ea-slim"

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
    tags = setOf(project.version.toString().substringBefore("+"), "latest")
  }

  container {
    appRoot = "/app"
    ports = listOf("8080", "9898")
    entrypoint =
        listOf(
            "java",
            "-cp",
            "@${appRoot}/jib-classpath-file",
            "@${appRoot}/jib-main-class-file",
        )
    environment =
        mapOf(
            "JDK_JAVA_OPTIONS" to
                buildList {
                      add("-javaagent:${appRoot}/otel/otel-javaagent.jar")
                      addAll(runJvmArgs.map { it.replace(tmp, "/tmp/") })
                    }
                    .joinToString(" "),
            "OTEL_EXPERIMENTAL_CONFIG_FILE" to "${appRoot}/otel/sdk-config.yaml",
            // "OTEL_JAVAAGENT_EXTENSIONS" to "${appRoot}/otel/extensions",
            // "OTEL_JAVAAGENT_ENABLED" to "true",
            // "OTEL_JAVAAGENT_LOGGING" to "application",
        )

    args = listOf(project.name, project.version.toString())
    mainClass = application.mainClass.get()
    expandClasspathDependencies = true
    format = ImageFormat.OCI
    labels = project.containerLabels
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
val composeWebApp by configurations.creating

tasks {
  val copyTasks =
      listOf(jsApp, wasmApp, composeWebApp).map { cnf ->
        val appResDir = Path(base = "app", cnf.name.removeSuffix("App"))
        register<Sync>("copy${cnf.name}") {
          from(cnf)
          into(processResources.map { it.destinationDir.toPath().resolve(appResDir) })
          duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
      }

  processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    dependsOn(copyTasks)
  }

  // Makes sure jte is generated before compilation
  withType<KspAATask>().configureEach { dependsOn(generateJte) }

  // publish { finalizedBy(jibDockerBuild) }
}

dependencies {
  implementation(projects.shared)
  implementation(projects.backend.data)
  implementation(projects.backend.profiling)
  implementation(projects.backend.security)

  // Server dependencies
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.di)
  implementation(libs.ktor.server.jetty.jakarta)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.server.metrics.micrometer)
  implementation(libs.ktor.server.call.logging)
  implementation(libs.ktor.server.call.id)
  implementation(libs.ktor.server.status.pages)
  implementation(libs.ktor.server.default.headers)
  implementation(libs.ktor.server.forwarded.header)
  implementation(libs.ktor.server.http.redirect)
  implementation(libs.ktor.server.request.validation)
  implementation(libs.ktor.server.compression)
  implementation(libs.ktor.server.cors)
  implementation(libs.ktor.server.hsts)
  implementation(libs.ktor.server.csrf)
  implementation(libs.ktor.server.rate.limit)
  implementation(libs.ktor.server.double.receive)
  implementation(libs.ktor.server.host.common)
  implementation(libs.ktor.server.auto.head)
  implementation(libs.ktor.server.partial.content)
  implementation(libs.ktor.server.resources)
  implementation(libs.ktor.server.auth)
  implementation(libs.ktor.server.auth.jwt)
  implementation(libs.ktor.server.websockets)
  implementation(libs.ktor.server.sse)
  implementation(libs.ktor.server.html)
  implementation(libs.ktor.serialization.json)
  implementation(libs.kotlinx.serialization.hocon)
  implementation(libs.kotlin.metadata.jvm)

  // OpenAPI
  implementation(libs.ktor.server.swagger)
  // implementation(libs.ktor.server.openapi)

  // Client dependencies
  implementation(libs.ktor.client.java)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.client.encoding)
  implementation(libs.ktor.client.logging)
  implementation(libs.ktor.client.resources)
  implementation(libs.ktor.client.auth)
  implementation(libs.ktor.client.websockets)

  // Database
  implementation(libs.exoquery.runner.jdbc)
  implementation(libs.postgresql)
  implementation(libs.hikariCP)
  implementation(libs.sherlock.sql)
  // implementation(libs.exposed.core)
  // implementation(libs.exposed.jdbc)
  // implementation(libs.exposed.dao)
  // implementation(libs.exposed.kotlin.datetime)

  // Scheduler
  implementation(libs.cardiologist)

  // Wasm
  implementation(libs.chicory.compiler)

  // Templating
  jteGenerate(libs.jte.models)
  implementation(libs.jte.runtime)
  // compileOnly(libs.jte.kotlin)
  implementation(libs.kotlinx.html)
  implementation(libs.kotlin.wrappers.css)
  implementation(libs.ktor.server.html)

  // OpenTelemetry
  // javaAgent(projects.backend.agent.otel)
  javaAgent(libs.otel.instr.javaagent)
  javaAgent(layout.files("src/main/resources/otel"))
  implementation(libs.bundles.otel)
  implementation(libs.ktor.cohort.core)
  implementation(libs.ktor.cohort.hikari)

  // Logging
  implementation(libs.logback.classic)

  // Testing
  testImplementation(libs.ktor.server.test.host)
  testImplementation(libs.testcontainers.junit5)
  testImplementation(libs.testcontainers.postgresql)
  testImplementation(libs.testcontainers.k3s)
  testImplementation(libs.okhttp.tls)
  testImplementation(libs.wiremock.standalone)
  testImplementation(libs.wiremock.kotlin)
  testImplementation(libs.kubernetes.client)
  testImplementation(libs.konsist)

  // Copy js, wasm, compose apps
  findProject(":web")?.let {
    jsApp(project(path = it.path, configuration = "jsApp"))
    wasmApp(project(path = it.path, configuration = "wasmApp"))
  }

  findProject(":compose:cmp")?.let {
    composeWebApp(project(path = it.path, configuration = "composeWebApp"))
  }

  // Specify the classifier using variantOf
  // implementation(variantOf(libs.lwjgl.opengl) { classifier("natives-linux") })

  //  constraints {
  //    implementation(libs.kotlinx.html.get().module.toString()) {
  //      version { strictly(libs.kotlinx.html.get().version.toString()) }
  //      because("Ktor Issue!")
  //    }
  //  }
}

// JS build output files.
// tasks.named(jsWebpack).get().outputs.files

// configurations.all {
//  resolutionStrategy {
//    force("org.bouncycastle:bcutil-jdk18on:${libs.versions.bc.asProvider().get()}")
//    force("org.bouncycastle:bcprov-jdk18on:${libs.versions.bc.asProvider().get()}")
//  }
// }
