plugins {
  plugins.kotlin.jvm
  plugins.publishing
}

dependencies {
  implementation(projects.common)
  compileOnly(kotlin("compiler-embeddable"))
  testImplementation(kotlin("compiler-embeddable"))
  // testImplementation("dev.zacsweers.kctfork:ksp:+")
}
