import common.addModules

plugins {
  application
  id("plugins.kotlin.mpp")
}

application {
  mainClass = "dev.suresh.ApplicationKt"
  applicationDefaultJvmArgs +=
      listOf(
          "--show-version",
          "--enable-preview",
          "--add-modules=$addModules",
          "--enable-native-access=ALL-UNNAMED",
      )
}

dependencies { jvmMainImplementation(libs.slf4j.api) }
