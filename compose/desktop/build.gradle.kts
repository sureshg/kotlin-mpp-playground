import common.jvmArguments
import common.kotlinVersion
import org.jetbrains.compose.desktop.application.dsl.TargetFormat.*
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
  plugins.kotlin.mpp
  plugins.publishing
  alias(libs.plugins.jetbrains.compose)
}

dependencies {
  commonMainImplementation(projects.shared)
  jvmMainImplementation(compose.desktop.currentOs)
  // jvmMainImplementation(compose.desktop.common)
  // jvmMainImplementation(compose.components.resources)
}

compose {
  kotlinCompilerPlugin = dependencies.compiler.forKotlin(kotlinVersion.get())
  kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=${kotlinVersion.get()}")

  platformTypes = platformTypes.get() - KotlinPlatformType.native

  desktop {
    application {
      mainClass = "MainKt"
      args += buildList { add(project.version.toString()) }
      jvmArgs += buildList {
        addAll(jvmArguments(appRun = true, headless = false))
        add("-Dskiko.fps.enabled=true")
        add("-Dskiko.fps.periodSeconds=2.0")
        add("-Dskiko.fps.longFrames.show=true")
      }

      nativeDistributions {
        targetFormats(Dmg, Msi, Deb)
        packageName = project.name
        packageVersion = "1.0.0"
        description = "Compose desktop App!"
        copyright = "© 2023 Suresh"
        vendor = "Suresh"
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

        val resRoot = kotlin.sourceSets.jvmMain.get().resources.srcDirs.first()

        macOS {
          iconFile.set(resRoot.resolve("icons/icon-mac.icns"))
          setDockNameSameAsPackageName = true
          bundleID = "${project.group}.${project.name}"

          notarization {
            appleID.set("test.app@example.com")
            password.set("@keychain:NOTARIZATION_PASSWORD")
          }
        }

        linux { iconFile.set(resRoot.resolve("icons/icon-linux.png")) }

        windows {
          iconFile.set(resRoot.resolve("icons/icon-windows.ico"))
          menuGroup = "Compose Desktop App"
          // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
          upgradeUuid = "18159785-d967-4CD2-8885-77BFA97CFA9F"
        }
      }
    }
  }
}
