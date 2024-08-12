plugins {
  plugins.kotlin.mpp
  plugins.publishing
  alias(libs.plugins.kotlin.compose.compiler)
  alias(libs.plugins.kobweb.application)
  alias(libs.plugins.kobwebx.markdown)
}

description = "Compose HTML App"

kobweb { app { index { description = "Powered by Kobweb (${libs.versions.kobweb})" } } }

kotlin {
  // configAsKobwebApplication(moduleName = project.name)

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
        // implementation(libs.silk.icons.fa)
      }
    }

    // jvmMain.dependencies {
    //   compileOnly(libs.kobweb.api)
    // }
  }
}
