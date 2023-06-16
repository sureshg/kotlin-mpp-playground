package common

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.*
import org.gradle.jvm.toolchain.*
import org.gradle.kotlin.dsl.assign
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

/** Java version properties. */
val Project.javaVersion
  get() = libs.versions.java.asProvider().map { JavaVersion.toVersion(it) }

val Project.javaRelease
  get() = javaVersion.map { it.majorVersion.toInt() }

val Project.toolchainVersion
  get() = javaVersion.map { JavaLanguageVersion.of(it.majorVersion) }

val Project.toolchainVendor
  get() = libs.versions.java.vendor.map(JvmVendorSpec::matching)

val Project.jvmArguments
  get() = libs.versions.java.jvmArguments.get().split(",", " ").filter { it.isNotBlank() }

val Project.addModules
  get() = libs.versions.java.addModules.get()

/** Kotlin version properties. */
val Project.kotlinVersion
  get() = libs.versions.kotlin.asProvider()

val Project.kotlinJvmTarget
  get() = libs.versions.kotlin.jvmtarget.map { JvmTarget.fromTarget(it) }

val Project.kotlinApiVersion
  get() = libs.versions.kotlin.api.version.map { KotlinVersion.fromVersion(it) }

val Project.kotlinLangVersion
  get() = libs.versions.kotlin.lang.version.map { KotlinVersion.fromVersion(it) }

/** Kotlin Dependencies extension functions. */
val Project.isKotlinMPP
  get() = plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")

val Project.isKotlinJvmProject
  get() = plugins.hasPlugin("org.jetbrains.kotlin.jvm")

val Project.isKotlinJsProject
  get() = plugins.hasPlugin("org.jetbrains.kotlin.js")

// https://kotlinlang.org/docs/multiplatform-set-up-targets.html#distinguish-several-targets-for-one-platform
val mppTargetAttr = Attribute.of("mpp.target.name", String::class.java)

var Project.mppTargetName: String
  get() = configurations.firstOrNull()?.attributes?.getAttribute(mppTargetAttr).orEmpty()
  set(value) {
    configurations.all {
      attributes.attribute(mppTargetAttr, value)
    }
  }

context(Project)
fun JavaToolchainSpec.configureJvmToolchain() {
  languageVersion = toolchainVersion
  vendor = toolchainVendor
}

context(Project)
fun JavaCompile.configureJavac() {
  options.apply {
    encoding = "UTF-8"
    release = javaRelease
    isIncremental = true
    isFork = true
    debugOptions.debugLevel = "source,lines,vars"
    // For Gradle worker daemon.
    forkOptions.jvmArgs?.addAll(jvmArguments)
    compilerArgs.addAll(
      jvmArguments +
              listOf(
                "-Xlint:all",
                "-parameters",
                "--add-modules=$addModules",
                // "-Xlint:-deprecation", // suppress deprecations
                // "-Xlint:lossy-conversions", // suppress lossy conversions
                // "-XX:+IgnoreUnrecognizedVMOptions",
                // "--add-exports",
                // "java.base/sun.nio.ch=ALL-UNNAMED",
                // "--patch-module",
                // "$moduleName=${sourceSets.main.get().output.asPath}"
              ),
    )
  }
}

context(Project)
fun KotlinCommonCompilerOptions.configureKotlinCommon() {
  apiVersion = kotlinApiVersion
  languageVersion = kotlinLangVersion
  progressiveMode = true
  allWarningsAsErrors = false
  suppressWarnings = false
  verbose = true
  freeCompilerArgs.addAll(
    "-Xcontext-receivers",
    "-Xallow-result-return-type",
  )
}

context(Project)
fun KotlinJvmCompilerOptions.configureKotlinJvm() {
  jvmTarget = kotlinJvmTarget
  apiVersion = kotlinApiVersion
  languageVersion = kotlinLangVersion
  javaParameters = true
  verbose = true
  allWarningsAsErrors = false
  suppressWarnings = false
  freeCompilerArgs.addAll(
    "-Xadd-modules=$addModules",
    "-Xjsr305=strict",
    "-Xjvm-default=all",
    "-Xassertions=jvm",
    "-Xcontext-receivers",
    "-Xallow-result-return-type",
    "-Xemit-jvm-type-annotations",
    "-Xjspecify-annotations=strict",
    "-Xextended-compiler-checks",
    // "-Xjdk-release=$javaVersion",
    // "-Xadd-modules=ALL-MODULE-PATH",
    // "-Xmodule-path=",
    // "-Xjvm-enable-preview",
    // "-Xjavac-arguments=\"--add-exports java.base/sun.nio.ch=ALL-UNNAMED\"",
    // "-Xexplicit-api={strict|warning|disable}",
    // "-Xgenerate-strict-metadata-version",
  )
}

context(Project)
fun LanguageSettingsBuilder.configureKotlinLang() {
  progressiveMode = true
  languageVersion = kotlinLangVersion.get().version
  optIn("kotlin.ExperimentalStdlibApi")
  optIn("kotlin.contracts.ExperimentalContracts")
  optIn("kotlin.ExperimentalUnsignedTypes")
  optIn("kotlin.io.path.ExperimentalPathApi")
  optIn("kotlin.time.ExperimentalTime")
  optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
  optIn("kotlinx.serialization.ExperimentalSerializationApi")
  optIn("kotlin.ExperimentalMultiplatform")
  optIn("kotlin.js.ExperimentalJsExport")
}

context(Project)
fun KotlinJvmTest.configureKotlinTest() {
  configureJavaTest()
}

context(Project)
fun Test.configureJavaTest() {
  useJUnitPlatform()
  jvmArgs(jvmArguments)
  reports.html.required = true
  maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
  testLogging {
    events =
      setOf(
        TestLogEvent.STANDARD_ERROR,
        TestLogEvent.FAILED,
        TestLogEvent.SKIPPED,
      )
    exceptionFormat = TestExceptionFormat.FULL
    showExceptions = true
    showCauses = true
    showStackTraces = true
    showStandardStreams = true
  }
}

context(Project)
fun KotlinJsOptions.configureKotlinJs() {
  // sourceMap = true
  // sourceMapEmbedSources = "always"
  // freeCompilerArgs += listOf("-Xir-per-module")
}

context(Project)
fun KotlinNpmInstallTask.configureKotlinNpm() {
  //args.add("--ignore-engines")
}
