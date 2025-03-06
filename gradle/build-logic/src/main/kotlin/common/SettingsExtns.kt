package common

import kotlin.text.toBoolean
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Provider

fun Settings.gradleBooleanProperty(name: String): Provider<Boolean> =
    providers.gradleProperty(name).map(String::toBoolean).orElse(false)

val Settings.isNativeTargetEnabled: Boolean
  get() = gradleBooleanProperty("kotlin.target.native.enabled").get()

val Settings.isWinTargetEnabled: Boolean
  get() = gradleBooleanProperty("kotlin.target.win.enabled").get()

val Settings.isComposeEnabled: Boolean
  get() = gradleBooleanProperty("compose.enabled").get()

val Settings.isSpringBootEnabled: Boolean
  get() = gradleBooleanProperty("springboot.enabled").get()
