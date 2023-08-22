import common.jvmArguments
import common.mppTargetName

plugins {
  application
  plugins.kotlin.jvm
  plugins.publishing
}

description = "Ktor backend application"

application {
  mainClass = "AppKt"
  applicationDefaultJvmArgs += jvmArguments(forAppRun = true)
}

mppTargetName = "jvm"

dependencies {
  implementation(projects.common)
  // Specify the classifier using variantOf
  // implementation(variantOf(libs.lwjgl.opengl) { classifier("natives-linux") })
}
