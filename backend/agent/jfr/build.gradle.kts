import common.*
import kotlin.collections.plus

plugins {
  dev.suresh.plugin.kotlin.jvm
  application
  com.gradleup.shadow
  dev.suresh.plugin.publishing
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
