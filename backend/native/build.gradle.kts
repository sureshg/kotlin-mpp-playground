import com.google.cloud.tools.jib.gradle.extension.nativeimage.JibNativeImageExtension
import common.githubUser

plugins {
  `kotlin-multiplatform`
  plugins.publishing
  com.google.cloud.tools.jib
}

description = "Ktor native application"

kotlin {
  // Remove all the pre-configured targets except common
  //  targets .filter { it.platformType != KotlinPlatformType.common }.forEach {
  //        targets.remove(it)
  // }

  macosArm64 {
    binaries { executable(listOf(DEBUG, RELEASE)) { entryPoint = "main" } }
    compilations.configureEach {
      compilerOptions.configure { freeCompilerArgs.add("-Xallocator=custom") }
    }
  }
  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain {
      dependencies {
        api(projects.common)
        api(libs.arrow.suspendapp.ktor)
      }
    }
  }
}

jib {
  from { image = "gcr.io/distroless/base" }

  to {
    image = "${project.githubUser}/${project.name}"
    tags = setOf("latest")
  }

  pluginExtensions {
    pluginExtension {
      implementation = JibNativeImageExtension::class.qualifiedName
      // Native image should be available in "layout.buildDirectory.dir("native/nativeCompile")"
      properties = mapOf("imageName" to "${project.name}.kexe")
    }
  }

  container { mainClass = "MainKt" }
}

// workaround for Jib
sourceSets.maybeCreate("main")
