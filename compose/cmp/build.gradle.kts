@file:OptIn(ExperimentalComposeLibrary::class)

import common.Platform
import common.jvmArguments
import java.time.Year
import kotlin.io.path.listDirectoryEntries
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat.*
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask

plugins {
  plugins.kotlin.mpp
  plugins.publishing
  alias(libs.plugins.kotlin.compose.compiler)
  alias(libs.plugins.jetbrains.compose)
  // alias(libs.plugins.detekt)
}

description = "Compose Desktop App!"

kotlin {
  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared)
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)

      implementation(libs.compose.navigation)
      implementation(libs.compose.lifecycle.viewmodel)
      implementation(libs.compose.material3.adaptive)
      implementation(libs.compose.material3.adaptive.layout)
      implementation(libs.compose.material3.adaptive.nav)
      implementation(libs.compose.material3.windowsize)
      implementation(libs.compose.kottie)

      // implementation(compose.materialIconsExtended)
      // project.dependencies.detektPlugins(libs.detekt.compose.rules)
    }

    commonTest.dependencies { implementation(compose.uiTest) }

    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(compose.desktop.components.animatedImage)
      implementation(libs.kotlinx.coroutines.swing)
      implementation(libs.slf4j.simple)
      implementation(libs.evas.compose)
    }

    // jsMain.dependencies { implementation(compose.html.core) }
  }
}

composeCompiler { reportsDestination = layout.buildDirectory.dir("compose_compiler") }

compose {
  desktop {
    application {
      mainClass = "MainKt"
      args += listOf(project.version.toString())
      jvmArgs += buildList {
        // $APPDIR macro is used by jpackage.
        // To debug _JAVA_LAUNCHER_DEBUG=1
        add("-splash:${'$'}APPDIR/resources/splash.jpg")
        addAll(jvmArguments(appRun = true, headless = false))
        // Resource localization
        // add("-Duser.language=en")

        // add("-Dskiko.fps.enabled=true")
        // add("-Dskiko.fps.periodSeconds=2.0")
        // add("-Dskiko.fps.longFrames.show=true")
      }

      buildTypes.release.proguard {
        isEnabled = false
        optimize = false
        obfuscate = false
        // configurationFiles.from(layout.projectDirectory.file("proguard-desktop.pro"))
      }

      nativeDistributions {
        targetFormats(Dmg, Msi, Deb)
        packageName = project.name
        packageVersion = "1.0.0"
        description = "Compose desktop App!"
        copyright = "© ${Year.now()} Suresh"
        vendor = "Suresh"
        appResourcesRootDir = layout.projectDirectory.dir("src/assets")
        // licenseFile = rootProject.file("LICENSE")

        modules(
            "java.instrument",
            "jdk.unsupported",
            "jdk.jfr",
            "jdk.management.jfr",
            "jdk.management.agent",
            "jdk.crypto.ec",
            "java.net.http",
            "jdk.incubator.vector",
        )

        macOS {
          appStore = false
          bundleID = "${project.group}.${project.name}"
          setDockNameSameAsPackageName = true
          iconFile = layout.projectDirectory.file("src/assets/icons/mac.icns")

          notarization {
            appleID = "test.app@example.com"
            password = "@keychain:NOTARIZATION_PASSWORD"
          }
        }

        linux { layout.projectDirectory.file("src/assets/icons/linux.png") }

        windows {
          menu = true
          menuGroup = "" // root
          perUserInstall = true
          // https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
          upgradeUuid = "b4397f87-3196-4991-96f3-0bd8b0adbdfd"
          iconFile = layout.projectDirectory.file("src/assets/icons/win.ico")
        }

        fileAssociation(
            mimeType = "text/kotlin",
            extension = "kot",
            description = "Kotlin Source File",
            macOSIconFile = project.file("src/assets/icons/mac.icns"),
            windowsIconFile = project.file("src/assets/icons/win.ico"),
            linuxIconFile = project.file("src/assets/icons/linux.png"),
        )

        // val resRoot = kotlin.sourceSets.jvmMain.get().resources.srcDirs.first()
        // resRoot.resolve("gif/particle.gif")
      }
    }
  }

  resources {
    publicResClass = true
    packageOfResClass = "dev.suresh.compose.res"
    // val sourceSet = kotlin.sourceSets.commonMain
    // customDirectory(
    //     sourceSetName = sourceSet.name,
    //     directoryProvider = layout.dir(sourceSet.map { it.resources.srcDirs.first() }))
  }
}

tasks {
  val renameDmg by
      registering(Copy::class) {
        group = "distribution"
        description = "Rename the DMG file"

        val packageDmg = named<AbstractJPackageTask>("packageReleaseDmg")
        // build/compose/binaries/main-release/dmg/*.dmg
        val fromFile =
            packageDmg.map {
              it.appImage
                  .get()
                  .dir("../dmg")
                  .asFile
                  .toPath()
                  .listDirectoryEntries("${project.name}*.dmg")
                  .single()
            }

        from(fromFile)
        into(fromFile.map { it.parent })
        rename { "${project.name}-${Platform.currentArch}-$version.dmg" }
      }
}
