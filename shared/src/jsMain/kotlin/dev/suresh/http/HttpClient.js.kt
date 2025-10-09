package dev.suresh.http

import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.*
import io.ktor.client.engine.js.*

actual fun httpClient(
    name: String,
    timeout: Timeout,
    retry: Retry,
    kLogger: KLogger,
    config: HttpClientConfigurer,
) = HttpClient(Js) { config(this) }
