@file:Suppress("UnstableApiUsage")

import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  idea
  `kotlin-dsl`
  embeddedKotlin("plugin.serialization")
  alias(libs.plugins.jte)
  alias(libs.plugins.bestpractices)
  alias(libs.plugins.benmanes)
  // alias(libs.plugins.kotlin.dsl)
}

/**
 * Java version used in Java toolchains and Kotlin compile JVM target for Gradle precompiled script
 * plugins.
 */
val dslJavaVersion = libs.versions.kotlin.dsl.jvmtarget

idea {
  module {
    isDownloadJavadoc = true
    isDownloadSources = true
  }
}

tasks {
  withType<KotlinCompile>().configureEach {
    compilerOptions {
      jvmTarget = dslJavaVersion.map(JvmTarget::fromTarget)
      freeCompilerArgs.addAll("-Xcontext-receivers")
    }
  }

  validatePlugins {
    failOnWarning = true
    enableStricterValidation = true
  }

  dependencyUpdates { checkConstraints = true }

  register("cleanAll") {
    description = "Cleans all projects"
    group = LifecycleBasePlugin.CLEAN_TASK_NAME

    allprojects.mapNotNull { it.tasks.findByName("clean") }.forEach { dependsOn(it) }
    doLast { delete(layout.buildDirectory) }
  }
}

kotlin {
  sourceSets.all {
    languageSettings.apply {
      optIn("kotlin.ExperimentalStdlibApi")
      optIn("kotlin.time.ExperimentalTime")
      optIn("kotlin.io.encoding.ExperimentalEncodingApi")
      optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
      optIn("kotlinx.serialization.ExperimentalSerializationApi")
      optIn("org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi")
      optIn("org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl")
      optIn("org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl")
      optIn("org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDceDsl")
    }
  }
}

gradlePlugin {
  plugins {

    // Re-exposure of plugin from dependency. Gradle doesn't expose the plugin itself.
    register("com.gradle.enterprise") {
      id = "com.gradle.enterprise"
      implementationClass = "com.gradle.enterprise.gradleplugin.GradleEnterprisePlugin"
      displayName = "Gradle Enterprise"
      description = "Gradle enterprise settings plugin re-exposed from dependency"
    }

    // A generic plugin for both project and settings
    register("Generic Plugin") {
      id = "plugins.generic"
      implementationClass = "plugins.GenericPlugin"
      displayName = "Generic plugin"
      description = "A plugin-aware pre-compiled generic plugin"
      tags = listOf("Generic Plugin", "build-logic")
    }

    // Dependency Reports plugin
    register("Dependency Reports") {
      id = "plugins.dependency.reports"
      implementationClass = "plugins.DepReportsPlugin"
      displayName = "Dependency Reports plugin"
      description = "A plugin to list all the resolved artifacts"
      tags = listOf("Dependency Reports", "build-logic")
    }

    // Uncomment the id to change plugin id for this pre-compiled plugin
    named("plugins.common") {
      // id = "build.plugins.common"
      displayName = "Common build-logic plugin"
      description = "Common pre-compiled script plugin"
      tags = listOf("Common Plugin", "build-logic")
    }

    // val settingsPlugin by registering {}
  }
}

// Jte is used for generating build config.
jte {
  contentType = gg.jte.ContentType.Plain
  sourceDirectory = sourceSets.main.map { it.resources.srcDirs.first().toPath() }
  generate()
  jteExtension("gg.jte.models.generator.ModelExtension") {
    property("language", "Kotlin")
    // property("interfaceAnnotation", "@foo.bar.MyAnnotation")
    // property("implementationAnnotation", "@foo.bar.MyAnnotation")
  }
  // jteExtension("gg.jte.nativeimage.NativeResourcesExtension")
  // binaryStaticContent = true
  // kotlinCompileArgs = arrayOf("-jvm-target", dslJavaVersion.get())
}

dependencies {
  // Hack to access version catalog from pre-compiled script plugins.
  // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
  implementation(platform(libs.kotlin.bom))
  implementation(kotlin("stdlib"))
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.collections.immutable)
  // Http client and JSON serialization
  implementation(libs.ktor.client.java)
  implementation(libs.ktor.client.content.negotiation)
  implementation(libs.ktor.client.encoding)
  implementation(libs.ktor.client.logging)
  implementation(libs.ktor.client.resources)
  implementation(libs.ktor.client.auth)
  implementation(libs.ktor.serialization.json)
  // Text styling
  implementation(libs.ajalt.mordant)
  // Exec Jar
  implementation(libs.build.zip.prefixer)
  // Templating
  implementation(libs.jte.runtime)
  jteGenerate(libs.jte.models)
  // compileOnly(libs.jte.kotlin)

  // External plugins deps to use in precompiled script plugins
  // https://docs.gradle.org/current/userguide/custom_plugins.html#applying_external_plugins_in_precompiled_script_plugins
  implementation(libs.build.kotlin)
  // OR implementation(kotlin("gradle-plugin"))
  implementation(libs.build.kotlin.ksp)
  implementation(libs.build.powerassert)
  implementation(libs.build.kotlinx.atomicfu)
  implementation(libs.build.kotlin.allopen)
  implementation(libs.build.kotlinx.serialization)
  implementation(libs.build.kotlinx.kover)
  implementation(libs.build.kotlinx.benchmark)
  implementation(libs.build.kotlinx.bincompat)
  implementation(libs.build.dokka.plugin)
  implementation(libs.build.dokka.base)
  implementation(libs.build.redacted.plugin)
  implementation(libs.build.gradle.enterprise)
  implementation(libs.build.nexus.plugin)
  implementation(libs.build.spotless.plugin)
  implementation(libs.build.shadow.plugin)
  implementation(libs.build.semver.plugin)
  implementation(libs.build.github.changelog)
  implementation(libs.build.benmanesversions)
  implementation(libs.build.dependencyanalysis)
  implementation(libs.build.foojay.resolver)
  implementation(libs.build.nativeimage.plugin)
  implementation(libs.build.modulegraph.plugin)
  implementation(libs.build.cash.molecule.plugin)
  implementation(libs.build.npm.publish.plugin)
  implementation(libs.build.mokkery.plugin)
  implementation(libs.build.jte.plugin)
  // implementation(libs.jte.native)
  implementation(libs.build.jib.plugin)
  implementation(libs.build.jib.nativeimage.extn)
  // For using kotlin-dsl in pre-compiled script plugins
  implementation("${libs.build.kotlin.dsl.get().module}:${expectedKotlinDslPluginsVersion}")
  // implementation(libs.build.includegit.plugin)
  // implementation(libs.build.cyclonedx.plugin)
  testImplementation(gradleTestKit())
}
