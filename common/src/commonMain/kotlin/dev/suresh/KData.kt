package dev.suresh

import dev.zacsweers.redacted.annotations.Redacted

data class KData(val name: String, val age: Int, @Redacted val password: String)
