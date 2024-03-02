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
    args = listOf(project.name, project.version.toString())
    expandClasspathDependencies = true
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
    mainClass = application.mainClass.get()
  }

  containerizingMode = "packaged"
}

// Configuration to copy JS/Wasm app to resources
val jsApp by configurations.creating
val wasmJsApp by configurations.creating

tasks {
  val copyJsApp by
      registering(Copy::class) {
        from(jsApp)
        into(processResources.map { it.destinationDir.toPath().resolve(Path("app", "js")) })
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }

  val copyWasmApp by
      registering(Copy::class) {
        from(wasmJsApp)
        into(processResources.map { it.destinationDir.toPath().resolve(Path("app", "wasm")) })
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }

  // Copy webapp to resources
  processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    dependsOn(copyJsApp, copyWasmApp)
  }

  // Makes sure jte is generated before compilation
  withType<KspAATask>().configureEach { dependsOn(generateJte) }

  // Enable compilation with Java Module System.
  compileJava {
    options.compilerArgumentProviders.add(
        object : CommandLineArgumentProvider {
          @InputFiles
          @PathSensitive(PathSensitivity.RELATIVE)
          val kotlinClasses = compileKotlin.flatMap { it.destinationDirectory }

          override fun asArguments() =
              listOf("--patch-module", "$group=${kotlinClasses.get().asFile.absolutePath}")
        })
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

  // Monitoring
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
  wasmJsApp(project(path = projects.web.wasm.dependencyProject.path, configuration = "wasmApp"))

  // Specify the classifier using variantOf
  // implementation(variantOf(libs.lwjgl.opengl) { classifier("natives-linux") })

  //  constraints {
  //    implementation(libs.kotlinx.html.get().module.toString()) {
  //      version { strictly(libs.kotlinx.html.get().version.toString()) }
  //      because("Ktor Issue!")
  //    }
  //  }
}
