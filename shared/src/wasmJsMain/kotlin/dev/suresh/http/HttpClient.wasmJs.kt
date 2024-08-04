package dev.suresh.http

import io.ktor.client.*
import io.ktor.client.engine.js.*

actual fun httpClient(
    name: String,
    timeout: Timeout,
    retry: Retry,
    config: HttpClientConfig<*>.() -> Unit,
) = HttpClient(Js) { config(this) }
