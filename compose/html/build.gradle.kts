plugins {
  plugins.kotlin.mpp
  plugins.publishing
  alias(libs.plugins.kotlin.compose.compiler)
  // alias(libs.plugins.kobweb.application)
  // alias(libs.plugins.kobwebx.markdown)
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.shared)
        implementation(libs.compose.runtime)
      }
    }

    jsMain {
      dependencies {
        implementation(libs.compose.html.core)
        implementation(libs.kobweb.core)
        implementation(libs.kobweb.silk)
        implementation(libs.kobwebx.markdown)
        // implementation(compose.html.svg)
        // implementation(libs.silk.icons.fa)
      }
    }
  }
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
