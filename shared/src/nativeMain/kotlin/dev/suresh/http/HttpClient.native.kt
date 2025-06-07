package dev.suresh.http

import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.*
import io.ktor.client.engine.curl.*

actual fun httpClient(
    name: String,
    timeout: Timeout,
    retry: Retry,
    kLogger: KLogger,
    config: HttpClientConfigurer
) =
    HttpClient(Curl) {
      config(this)
      engine {
        // https://youtrack.jetbrains.com/issue/KTOR-8339
        if (Platform.osFamily == OsFamily.LINUX) {
          caPath = "/etc/ssl/certs"
        }
        sslVerify = true
      }
    }
