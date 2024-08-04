package dev.suresh.http

import io.ktor.client.*
import io.ktor.client.engine.java.*

actual fun httpClient(
    name: String,
    timeout: Timeout,
    retry: Retry,
    config: HttpClientConfig<*>.() -> Unit
) = HttpClient(Java) { config(this) }
