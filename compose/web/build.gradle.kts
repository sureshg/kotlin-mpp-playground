import common.kotlinVersion

plugins {
  plugins.kotlin.mpp
  plugins.publishing
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.kobweb.application)
  alias(libs.plugins.kobwebx.markdown)
}

kobweb { app { index { this.description.set("Kobweb!") } } }

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.shared)
        implementation(compose.runtime)
      }
    }

    jsMain {
      dependencies {
        implementation(compose.html.core)
        implementation(libs.kobweb.core)
        implementation(libs.kobweb.silk)
        implementation(libs.kobwebx.markdown)
        // implementation(compose.html.svg)
        // implementation(libs.silk.icons.fa)
      }
    }
  }
}

compose {
  kotlinCompilerPlugin = dependencies.compiler.forKotlin(kotlinVersion.get())
  // kotlinCompilerPlugin = libs.versions.jetbrains.compose.compiler
  kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=${kotlinVersion.get()}")
}

tasks {
  jsProcessResources {
    inputs.property("version", project.version.toString())
    filesMatching("manifest.json") {
      expand(
          "name" to project.name,
          "version" to project.version.toString().substringBeforeLast("."),
      )
    }
  }
}
