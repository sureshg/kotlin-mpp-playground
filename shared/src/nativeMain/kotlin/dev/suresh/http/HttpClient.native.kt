package dev.suresh.http

import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.*
import io.ktor.client.engine.cio.*

actual fun httpClient(
    name: String,
    timeout: Timeout,
    retry: Retry,
    httpLogger: KLogger,
    config: HttpClientConfig<*>.() -> Unit
) = HttpClient(CIO) { config(this) }
