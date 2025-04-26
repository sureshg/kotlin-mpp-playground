package common

import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Provider

fun Settings.gradleBooleanProp(name: String): Provider<Boolean> =
    providers.gradleProperty(name).map(String::toBoolean).orElse(false)

val Settings.isNativeTargetEnabled: Boolean
  get() = gradleBooleanProp("kotlin.target.native.enabled").get()

val Settings.isWinTargetEnabled: Boolean
  get() = gradleBooleanProp("kotlin.target.win.enabled").get()

val Settings.isComposeModuleEnabled: Boolean
  get() = gradleBooleanProp("module.compose.enabled").get()

val Settings.isBootModuleEnabled: Boolean
  get() = gradleBooleanProp("module.boot.enabled").get()
