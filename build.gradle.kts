import common.commonProjectName

plugins {
  plugins.common
  plugins.misc
  kotlin("multiplatform") apply false
}

// Multi module single coverage report
dependencies {
  kover(project(":$commonProjectName"))
  kover(project("backend"))
  kover(project("web"))
}
