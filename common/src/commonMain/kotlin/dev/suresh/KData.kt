package dev.suresh

import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.Serializable

@Serializable data class KData(val name: String, val age: Int, @Redacted val password: String)
