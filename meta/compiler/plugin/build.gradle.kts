plugins {
  plugins.kotlin.jvm
  plugins.publishing
}

dependencies {
  implementation(projects.shared)
  compileOnly(kotlin("compiler-embeddable"))
  testImplementation(kotlin("compiler-embeddable"))
  // testImplementation("dev.zacsweers.kctfork:ksp:+")
}
