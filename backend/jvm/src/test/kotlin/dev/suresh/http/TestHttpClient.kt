package dev.suresh.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import nl.altindag.ssl.SSLFactory

val testSSLFactory by lazy { SSLFactory.builder().withUnsafeTrustMaterial().build() }

val testHttpClient by lazy {
  HttpClient(Java) { engine { config { sslContext(testSSLFactory.sslContext) } } }
}
