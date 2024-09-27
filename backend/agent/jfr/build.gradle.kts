import common.jvmRunArgs
import kotlin.collections.plus

plugins {
  plugins.kotlin.jvm
  application
  com.gradleup.shadow
}

description = "JVM JFR Agent!"

application {
  mainClass = libs.versions.app.mainclass.get()
  applicationDefaultJvmArgs += project.jvmRunArgs
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
