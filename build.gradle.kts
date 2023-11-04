plugins {
  plugins.common
  plugins.misc
  alias(libs.plugins.jetbrains.compose) apply false
  // kotlin("multiplatform") apply false
}

description = "Kotlin Multiplatform Playground!"
