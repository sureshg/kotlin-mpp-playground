import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  plugins.kotlin.mpp
  plugins.publishing
  alias(libs.plugins.jetbrains.compose.mpp)
}

dependencies {
  commonMainImplementation(projects.common)
  // jvmMainImplementation(compose.desktop.common)
  jvmMainImplementation(compose.desktop.currentOs)
}

compose.desktop {
  application {
    mainClass = "MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = project.name
      packageVersion = "1.0.0"
    }
  }
}
