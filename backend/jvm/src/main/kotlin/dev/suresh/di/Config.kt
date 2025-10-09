package dev.suresh.di

import dev.zacsweers.redacted.annotations.Redacted
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class Auth(val api: API, val admin: Admin)

@Serializable
data class API(val user: String, @Redacted @SerialName("bearer-token") val bearerToken: String)

@Serializable data class Admin(val user: String, @Redacted val password: String)
