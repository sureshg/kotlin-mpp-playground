package dev.suresh

import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.marcinziolo.kotlin.wiremock.get
import com.marcinziolo.kotlin.wiremock.like
import com.marcinziolo.kotlin.wiremock.returns
import dev.suresh.http.testHttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.RegisterExtension

@EnabledIfSystemProperty(named = "jdk.net.hosts.file", matches = "true")
class DnsTest {

  companion object {

    @JvmField
    @RegisterExtension
    val wireMock =
        WireMockExtension.newInstance()
            .options(
                wireMockConfig().httpDisabled(true).httpsPort(8888)
                // .keystorePath("src/test/resources/keystore.jks")
                // .keystorePassword("changeit")
                // .keyManagerPassword("changeit")
                // .keystoreType("PKCS12")
                // .trustStorePath("src/test/resources/truststore.jks")
                // .trustStorePassword("changeit")
                // .trustStoreType("PKCS12")
                // .needClientAuth(true)
                )
            .build()
  }

  @Test
  fun hostFileTest() = runTest {
    val addr = InetAddress.getByName("test.dev")
    assertEquals("127.0.0.1", addr.hostAddress, "Address should be 127.0.0.1")
  }

  @Test
  fun wireMockTest() = runTest {
    wireMock.get { url like "/users/.*" } returns
        {
          header = "Content-Type" to "application/json"
          statusCode = 200
          body =
              """
              {
                "id": 1,
                "name": "Suresh"
              }
              """
        }

    val client = testHttpClient.config { defaultRequest { url(wireMock.baseUrl()) } }
    println(client.get("/users/1").bodyAsText())
  }
}
