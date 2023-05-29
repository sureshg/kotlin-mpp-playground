import dev.suresh.gradle.addModules
import dev.suresh.gradle.maybeRegister
import tasks.BuildConfig

plugins {
  application
  id("plugins.kotlinMPP")
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

tasks {
  // Add generated buildConfig to commonMain sourceSet
  val buildConfig by registering(BuildConfig::class) { classFqName = "BuildConfig" }
  kotlin.sourceSets.commonMain.configure { kotlin.srcDirs(buildConfig) }
  maybeRegister<Task>("prepareKotlinIdeaImport") { dependsOn(buildConfig) }
}

dependencies { jvmMainImplementation(libs.slf4j.api) }
