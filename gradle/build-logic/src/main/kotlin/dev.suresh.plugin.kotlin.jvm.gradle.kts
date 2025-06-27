import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.cloud.tools.jib.gradle.JibTask
import common.*
import java.io.*
import java.util.spi.ToolProvider
import kotlinx.validation.*
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.*
import tasks.*

plugins {
  `java-library`
  kotlin("jvm")
  kotlin("plugin.serialization")
  kotlin("plugin.power-assert")
  com.google.devtools.ksp
  dev.zacsweers.redacted
  // com.javiersc.kotlin.kopy
  id("dev.suresh.plugin.common")
  id("dev.suresh.plugin.kotlin.docs")
  // `test-suite-base`
}

// Apply the regular plugin
apply(plugin = "dev.suresh.plugin.depreports")

// Load the build script from a file
// apply(from = rootDir.resolve("project.plugin.gradle.kts"))

configurations.configureEach {
  resolutionStrategy {
    // failOnNonReproducibleResolution()
    // eachDependency { if (requested.name.contains("intellij-coverage")) {
    // useVersion(libs.versions.intellij.coverage.get()) }}
  }
}

java {
  toolchain { configureJvmToolchain(project) }
  // withSourcesJar()
  // withJavadocJar()
}

kotlin {
  jvmToolchain { configureJvmToolchain(project) }
  compilerOptions {
    configureKotlinCommon(project)
    configureKotlinJvm(project)
  }
  // sourceSets.all { kotlin.setSrcDirs(listOf("src/kotlin")) }
}

@Suppress("UnstableApiUsage", "UNUSED_VARIABLE")
testing {
  suites.withType<JvmTestSuite> {
    useJUnitJupiter(libs.versions.junit)
    // Configure all test suites
    targets.configureEach { testTask { configureJavaTest() } }
  }
}

ksp { allWarningsAsErrors = false }

powerAssert {
  functions =
      listOf(
          "kotlin.assert",
          "kotlin.test.assertTrue",
          "kotlin.test.assertEquals",
          "kotlin.test.assertNull",
          "kotlin.require")
}

redacted {
  enabled = true
  replacementString = "█"
}

// kopy { copyFunctions = listOf(KopyCopyFunctions.Copy) }

// Java agent configuration for jib
val javaAgent by configurations.registering { isTransitive = false }

tasks {
  val buildConfigExtn = extensions.create<BuildConfigExtension>("buildConfig")
  val buildConfig = register<BuildConfig>("buildConfig", buildConfigExtn)
  buildConfig.configure { enabled = buildConfigExtn.enabled.get() }
  kotlin.sourceSets.main { kotlin.srcDirs(buildConfig) }

  // Configure "compileJava" and "compileTestJava" tasks.
  withType<JavaCompile>().configureEach { configureJavac(project) }

  // withType<KotlinJvmCompile>().configureEach { finalizedBy("spotlessApply") }

  // Configure jvm args for JavaExec tasks except `run`
  withType<JavaExec>().matching { it.name != "run" }.configureEach { jvmArgs(defaultJvmArgs) }

  withType<Jar>().configureEach {
    manifest { attributes(defaultJarManifest) }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
  }

  processResources {
    val version = project.version.toString()
    val rootProjectName = rootProject.name
    val moduleName = project.name

    inputs.property("version", version)
    filesMatching("**/*-res.txt") {
      expand(
          "name" to rootProjectName,
          "module" to moduleName,
          "version" to version,
      )
    }
    filesMatching("**/*.yaml") {
      filter { line ->
        line.replace("{project.name}", rootProjectName).replace("{project.version}", version)
      }
    }
  }

  javadoc {
    isFailOnError = true
    (options as StandardJavadocDocletOptions).apply {
      encoding = Charsets.UTF_8.name()
      linkSource(true)
      addBooleanOption("-enable-preview", true)
      if (addModules.isNotBlank()) {
        addStringOption("-add-modules", addModules)
      }
      addStringOption("-release", javaRelease.get().toString())
      addStringOption("Xdoclint:none", "-quiet")
    }
    // exclude("**/Main.java")
  }

  pluginManager.withPlugin("com.gradleup.shadow") {
    val buildExecutable by
        registering(ReallyExecJar::class) {
          jarFile = named<ShadowJar>("shadowJar").flatMap { it.archiveFile }
          javaOpts = runJvmArgs
          execJarFile = layout.buildDirectory.dir("libs").map { it.file(project.name) }
          onlyIf { OperatingSystem.current().isUnix }
        }

    build { finalizedBy(buildExecutable) }

    register("printModuleDeps") {
      description = "Print Java Platform Module dependencies of the application."
      group = LifecycleBasePlugin.BUILD_GROUP

      val shadowJar = named<ShadowJar>("shadowJar")
      doLast {
        val jarFile = shadowJar.get().archiveFile.get().asFile

        val jdeps =
            ToolProvider.findFirst("jdeps").orElseGet { error("jdeps tool is missing in the JDK!") }
        val out = StringWriter()
        val pw = PrintWriter(out)
        jdeps.run(
            pw,
            pw,
            "-q",
            "-R",
            "--print-module-deps",
            "--ignore-missing-deps",
            "--multi-release=${javaRelease.get()}",
            jarFile.absolutePath,
        )

        val modules = out.toString()
        logger.quiet(
            """
            |Application modules for OpenJDK-${javaRelease.get()} are,
            |${modules.split(",").mapIndexed { i, module -> " ${(i + 1).toString()
                      .padStart(2)}) $module" }.joinToString(System.lineSeparator())}
            """
                .trimMargin())
      }
      dependsOn(shadowJar)
    }

    val jdepExtn = extensions.create<JdeprscanExtension>("jdeprscan")
    register<Jdeprscan>("jdeprscan", jdepExtn).configure {
      jarFile = named<ShadowJar>("shadowJar").flatMap { it.archiveFile }
    }
  }

  // Copy OpenTelemetry Java agent for jib
  pluginManager.withPlugin("com.google.cloud.tools.jib") {
    val copyOtelAgent by
        registering(Copy::class) {
          from(javaAgent) {
            eachFile {
              if (name.startsWith("opentelemetry-javaagent")) {
                name = "otel-javaagent.jar"
              }
            }
          }
          into(layout.buildDirectory.dir("otel"))
          duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

    processResources { dependsOn(copyOtelAgent) }

    // Disable configuration cache for Jib
    withType<JibTask>().configureEach {
      notCompatibleWithConfigurationCache(
          "because https://github.com/GoogleContainerTools/jib/issues/3132")
    }
  }

  pluginManager.withPlugin("org.jetbrains.kotlinx.binary-compatibility-validator") {
    configure<ApiValidationExtension> {
      ignoredPackages.add("dev.suresh.test")
      ignoredClasses.addAll(listOf("BuildConfig", "BuildConfig\$Host"))
      validationDisabled = false
      klib { enabled = true }
    }

    withType<KotlinApiBuildTask>().configureEach {
      // inputJar = named<Jar>("shadowJar").flatMap { it.archiveFile }
    }
  }
}

dependencies {
  implementation(platform(libs.kotlin.bom))
  implementation(platform(libs.kotlinx.coroutines.bom))
  implementation(platform(libs.kotlinx.serialization.bom))
  implementation(platform(libs.ktor.bom))
  implementation(platform(libs.kotlin.wrappers.bom))
  implementation(kotlin("stdlib"))
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.io.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.serialization.json.io)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.collections.immutable)
  implementation(libs.kotlin.redacted.annotations)
  implementation(libs.jspecify)
  implementation(libs.bundles.keystore)
  implementation(libs.slf4j.api)
  // implementation(libs.slf4j.jul)

  // Auto-service
  ksp(libs.ksp.auto.service)
  implementation(libs.google.auto.annotations)

  // Test dependencies
  testImplementation(platform(libs.junit.bom))
  testImplementation(platform(libs.testcontainers.bom))
  testImplementation(kotlin("reflect"))
  testImplementation(kotlin("test-junit5"))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.kotlinx.lincheck)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.mockk)
  // testImplementation(libs.slf4j.simple)
}
