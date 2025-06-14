package dev.suresh.http

import io.github.oshai.kotlinlogging.*
import io.ktor.client.*
import io.ktor.client.engine.curl.*
import kotlinx.io.files.*

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
        val cacertBundle = "/etc/ssl/certs/ca-certificates.crt"
        if (Platform.osFamily == OsFamily.LINUX && SystemFileSystem.exists(Path(cacertBundle))) {
          caInfo = cacertBundle
          kLogger.warn { "Setting cacertBundle to $caInfo" }
        }
        sslVerify = true
      }
    }
