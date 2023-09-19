import common.jvmArguments
import common.mppTargetName

plugins {
  plugins.kotlin.jvm
  plugins.publishing
  application
}

description = "Ktor backend jvm application"

mppTargetName = "jvm"

application {
  mainClass = "AppKt"
  applicationDefaultJvmArgs += jvmArguments(forAppRun = true)
}

dependencies {
  implementation(projects.common)
  implementation(libs.logback.classic)
  // Specify the classifier using variantOf
  // implementation(variantOf(libs.lwjgl.opengl) { classifier("natives-linux") })
}
