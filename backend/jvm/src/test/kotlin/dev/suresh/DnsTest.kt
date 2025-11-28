package dev.suresh

import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.condition.EnabledIfSystemProperty

@EnabledIfSystemProperty(named = "jdk.net.hosts.file", matches = "true")
class DnsTest {

  @Test
  fun hostFileTest() = runTest {
    val addr = InetAddress.getByName("test.dev")
    assertEquals("127.0.0.1", addr.hostAddress, "Address should be 127.0.0.1")
  }

  @Test
  fun wireMockTest() = runTest {
    // wireMock.get { url like "/users/.*" } returns
    //     {
    //       header = "Content-Type" to "application/json"
    //       statusCode = 200
    //       body =
    //           """
    //           {
    //             "id": 1,
    //             "name": "Suresh"
    //           }
    //           """
    //     }
    //
    // val client = testHttpClient.config { defaultRequest { url(wireMock.baseUrl()) } }
    // println(client.get("/users/1").bodyAsText())
  }
}
