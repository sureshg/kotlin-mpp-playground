package dev.suresh.cert

import dev.suresh.tls.SavingTrustManager
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object CertScan {

  fun scan(
      host: String,
      port: Int = 443,
      sni: String? = null,
      timeout: Duration = 2_000.milliseconds
  ): List<X509Certificate> {
    val trustManager = SavingTrustManager()
    val socket =
        SSLContext.getInstance("TLS").run {
          init(null, arrayOf(trustManager), null)
          socketFactory.createSocket() as SSLSocket
        }

    return socket.use { sock ->
      val handshake = runCatching {
        sni?.let {
          // sock.sslParameters will create a new object
          val sslParams = sock.sslParameters
          sslParams.serverNames = listOf(SNIHostName(sni))
          sock.sslParameters = sslParams
        }
        sock.soTimeout = timeout.inWholeMilliseconds.toInt()
        sock.connect(InetSocketAddress(host, port), timeout.inWholeMilliseconds.toInt())
        sock.startHandshake()

        // Peer has to be authenticated this to work
        // sock.session.peerCertificates.filterIsInstance<X509Certificate>()
      }
      trustManager.chain
    }
  }
}
