import common.*
import kotlin.collections.plus

plugins {
  id("dev.suresh.plugin.kotlin.jvm")
  id("dev.suresh.plugin.publishing")
  application
  id("com.gradleup.shadow")
}

description = "JVM JFR Agent!"

application {
  mainClass = libs.versions.app.mainclass.get()
  applicationDefaultJvmArgs += ""
}

tasks {
  jar {
    manifest {
      attributes(
          "Premain-Class" to application.mainClass,
          "Agent-Class" to application.mainClass,
          "Launcher-Agent-Class" to application.mainClass,
          "Can-Redefine-Classes" to "true",
          "Can-Retransform-Classes" to "true",
          "Can-Set-Native-Method-Prefix" to "true",
          "Implementation-Title" to project.name,
          "Implementation-Version" to version,
      )
    }
  }
}
