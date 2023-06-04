import common.addModules

plugins {
  application
  id("plugins.kotlin.jvm")
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
