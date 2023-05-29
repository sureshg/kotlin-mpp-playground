import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `kotlin-dsl`
  alias(libs.plugins.jte)
  alias(libs.plugins.bestpractices)
}

/**
 * Java version used in Java toolchains and Kotlin compile JVM target for Gradle precompiled script
 * plugins.
 */
val dslJavaVersion = libs.versions.kotlin.dsl.jvmtarget

tasks {
  withType<KotlinCompile>().configureEach {
    compilerOptions { jvmTarget = dslJavaVersion.map(JvmTarget::fromTarget) }
  }
}

kotlin {
  sourceSets.all {
    languageSettings.apply {
      progressiveMode = true
      optIn("kotlin.ExperimentalStdlibApi")
      optIn("kotlin.io.path.ExperimentalPathApi")
      optIn("kotlin.time.ExperimentalTime")
    }
  }
}

gradlePlugin {
  plugins {
    // Uncomment the id to change plugin id for this pre-compiled plugin
    named("plugins.common") {
      // id = "dev.suresh.gradle.plugins.common"
      displayName = "Common plugin"
      description = "Common pre-compiled script plugin"
      tags = listOf("Common Plugin")
    }
  }
}

// Jte is used for generating build config.
jte {
  contentType = gg.jte.ContentType.Plain
  sourceDirectory = sourceSets.main.get().resources.srcDirs.firstOrNull()?.toPath()
  generate()
}

dependencies {
  implementation(platform(libs.kotlin.bom))
  implementation(kotlin("stdlib"))
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
  implementation(libs.ajalt.mordant)
  implementation(libs.jte.runtime)
  implementation(libs.build.zip.prefixer)
  // compileOnly(libs.jte.kotlin)

  implementation(libs.build.kotlin)
  // OR implementation(kotlin("gradle-plugin"))
  implementation(libs.build.kotlin.ksp)
  implementation(libs.build.kotlinx.atomicfu)
  implementation(libs.build.kotlinx.serialization)
  implementation(libs.build.kotlinx.kover)
  implementation(libs.build.dokka)
  implementation(libs.build.ksp.redacted)
  implementation(libs.build.nexus.plugin)
  implementation(libs.build.spotless.plugin)
  implementation(libs.build.shadow.plugin)
  implementation(libs.build.semver.plugin)
  implementation(libs.build.benmanesversions)
  implementation(libs.build.dependencyanalysis)
  implementation(libs.build.foojay.resolver)
  testImplementation(gradleTestKit())
}
