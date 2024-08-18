package dev.suresh.http

import dev.suresh.cert.RootCA
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.java.*
import nl.altindag.ssl.SSLFactory

val log = KotlinLogging.logger {}

val customSSLFactory by lazy {
  log.info { "Initializing TLS context with custom RootCAs..." }
  log.info { "Root CAs: ${RootCA.commonNames}" }
  SSLFactory.builder()
      .withDefaultTrustMaterial()
      .withTrustMaterial(RootCA.certs)
      .withSwappableTrustMaterial()
      .build()
}

actual fun httpClient(
    name: String,
    timeout: Timeout,
    retry: Retry,
    httpLogger: KLogger,
    config: HttpClientConfig<*>.() -> Unit
) =
    HttpClient(Java) {
      config(this)
      engine { config { sslContext(customSSLFactory.sslContext) } }
    }
