import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
  plugins.kotlin.mpp
  plugins.publishing
  alias(libs.plugins.kotlin.compose.compiler)
  alias(libs.plugins.jetbrains.compose)
  // alias(libs.plugins.kobweb.application)
  // alias(libs.plugins.kobwebx.markdown)
}

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

composeCompiler {
  enableStrongSkippingMode = true
  reportsDestination = layout.buildDirectory.dir("compose_compiler")
  targetKotlinPlatforms = setOf(KotlinPlatformType.js)
}

// kobweb { app { index { this.description.set("Kobweb!") } } }

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
