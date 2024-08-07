package dev.suresh.http

import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.*

actual fun httpClient(
    name: String,
    timeout: Timeout,
    retry: Retry,
    httpLogger: KLogger,
    config: HttpClientConfig<*>.() -> Unit
) = HttpClient { config(this) }
