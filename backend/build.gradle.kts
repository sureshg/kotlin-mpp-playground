import dev.suresh.gradle.addModules

plugins {
  application
  id("plugins.kotlinJvm")
}

application {
  mainClass = "AppKt"
  applicationDefaultJvmArgs +=
      listOf(
          "--show-version",
          "--enable-preview",
          "--add-modules=$addModules",
          "--enable-native-access=ALL-UNNAMED",
      )
}

dependencies { implementation(projects.common) }
