plugins {
  plugins.kotlin.jvm
  plugins.publishing
}

dependencies {
  compileOnly(kotlin("compiler-embeddable"))
  testImplementation(kotlin("compiler-embeddable"))
  testImplementation("com.github.tschuchortdev:kotlin-compile-testing:+")
}
