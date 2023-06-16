import common.addModules
import common.mppTargetName

plugins {
  application
  plugins.kotlin.jvm
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

mppTargetName = "jvm"

dependencies { implementation(projects.common) }
