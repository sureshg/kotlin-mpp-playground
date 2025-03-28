package common

import kotlin.text.toBoolean
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Provider

fun Settings.gradleBooleanProp(name: String): Provider<Boolean> =
    providers.gradleProperty(name).map(String::toBoolean).orElse(false)

val Settings.isNativeTargetEnabled: Boolean
  get() = gradleBooleanProp("kotlin.target.native.enabled").get()

val Settings.isWinTargetEnabled: Boolean
  get() = gradleBooleanProp("kotlin.target.win.enabled").get()

val Settings.isComposeEnabled: Boolean
  get() = gradleBooleanProp("compose.enabled").get()

val Settings.isSpringBootEnabled: Boolean
  get() = gradleBooleanProp("springboot.enabled").get()
