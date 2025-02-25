import com.github.ajalt.mordant.rendering.TextColors
import com.google.cloud.tools.jib.gradle.BuildDockerTask
import com.google.devtools.ksp.gradle.KspAATask
import com.javiersc.kotlin.kopy.args.KopyFunctions
import common.*
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.jar.Attributes
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
  com.javiersc.kotlin.kopy
  org.jetbrains.kotlinx.atomicfu
  id("dev.suresh.plugin.common")
  id("dev.suresh.plugin.kotlin.docs")
  // kotlin("plugin.atomicfu")
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
  withSourcesJar()
  withJavadocJar()
  toolchain { configureJvmToolchain(project) }
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

atomicfu {
  transformJvm = true
  jvmVariant = "VH"
}

ksp {
  arg("autoserviceKsp.verify", "true")
  arg("autoserviceKsp.verbose", "true")
  allWarningsAsErrors = false
}

powerAssert { functions = listOf("kotlin.assert", "kotlin.test.assertTrue") }

redacted {
  enabled = true
  replacementString = "█"
}

kopy { functions = KopyFunctions.Copy }

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
  withType<JavaExec>().matching { it.name != "run" }.configureEach { jvmArgs(jvmArguments()) }

  // Configure KSP2
  withType<KspAATask>().configureEach { configureKspConfig() }

  withType<Jar>().configureEach {
    manifest {
      attributes(
          // "Automatic-Module-Name" to project.group,
          "Enable-Native-Access" to "ALL-UNNAMED",
          "Built-By" to System.getProperty("user.name"),
          "Built-Jdk" to System.getProperty("java.runtime.version"),
          "Built-OS" to
              "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}",
          "Build-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.now()),
          "Created-By" to "Gradle ${gradle.gradleVersion}",
          Attributes.Name.IMPLEMENTATION_TITLE.toString() to project.name,
          Attributes.Name.IMPLEMENTATION_VERSION.toString() to project.version,
          Attributes.Name.IMPLEMENTATION_VENDOR.toString() to project.group,
      )
    }
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
      addStringOption("-add-modules", addModules)
      addStringOption("-release", javaRelease.get().toString())
      addStringOption("Xdoclint:none", "-quiet")
    }
    exclude("**/Main.java")
  }

  pluginManager.withPlugin("com.gradleup.shadow") {
    val buildExecutable by
        registering(ReallyExecJar::class) {
          val shadowJar by existing(Jar::class)
          jarFile = shadowJar.flatMap { it.archiveFile }
          // javaOpts = application.applicationDefaultJvmArgs
          javaOpts = named<JavaExec>("run").get().jvmArgs
          execJarFile = layout.buildDirectory.dir("libs").map { it.file("${project.name}-app") }
          onlyIf { OperatingSystem.current().isUnix }
        }

    build { finalizedBy(buildExecutable) }

    register("printModuleDeps") {
      description = "Print Java Platform Module dependencies of the application."
      group = LifecycleBasePlugin.BUILD_GROUP

      val shadowJar by existing(Jar::class)
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
            |${modules.split(",")
                        .mapIndexed { i, module -> " ${(i + 1).toString().padStart(2)}) $module" }
                        .joinToString(System.lineSeparator())}
            """
                .trimMargin())
      }
      dependsOn(shadowJar)
    }

    val jdepExtn = extensions.create<JdeprscanExtension>("jdeprscan")
    register<Jdeprscan>("jdeprscan", jdepExtn).configure {
      val shadowJar by existing(Jar::class)
      jarFile = shadowJar.flatMap { it.archiveFile }
    }
  }

  // Copy OpenTelemetry Java agent for jib
  pluginManager.withPlugin("com.google.cloud.tools.jib") {
    val copyOtelAgent by
        registering(Copy::class) {
          from(javaAgent)
          into(layout.buildDirectory.dir("otel"))
          rename { "otel-javaagent.jar" }
          duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

    processResources { dependsOn(copyOtelAgent) }

    // Docker command to run the image
    withType<BuildDockerTask>().configureEach {
      doLast {
        val portMapping = jib?.container?.ports.orEmpty().joinToString(" ") { "-p $it:$it" }
        val image = jib?.to?.image ?: project.name
        val tag = jib?.to?.tags?.firstOrNull() ?: "latest"
        val env =
            jib?.container
                ?.environment
                .orEmpty()
                .map { "-e ${it.key}=${it.value}" }
                .joinToString(" ")
        logger.lifecycle(
            TextColors.cyan(
                """
                |Run: docker run -it --rm --name ${project.name} $portMapping $env $image:$tag
                """
                    .trimMargin()))
      }
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
  implementation(libs.bundles.ajalt)
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
