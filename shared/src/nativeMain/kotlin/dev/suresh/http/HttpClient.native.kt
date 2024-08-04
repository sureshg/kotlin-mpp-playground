package dev.suresh.http

import io.ktor.client.*

actual fun httpClient(
    name: String,
    timeout: Timeout,
    retry: Retry,
    config: HttpClientConfig<*>.() -> Unit
) = HttpClient { config(this) }
