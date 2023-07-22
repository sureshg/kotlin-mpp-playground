import common.jvmArguments
import common.mppTargetName

plugins {
  application
  plugins.kotlin.jvm
}

description = "Ktor backend application"

application {
  mainClass = "AppKt"
  applicationDefaultJvmArgs += jvmArguments()
}

mppTargetName = "jvm"

dependencies {
  implementation(projects.common)
  // Specify the classifier using variantOf
  // implementation(variantOf(libs.lwjgl.opengl) { classifier("natives-linux") })
}
