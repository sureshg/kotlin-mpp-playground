package dev.suresh.cert

import dev.suresh.tls.SavingTrustManager
import dev.suresh.tls.newTLSSocket
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import javax.net.ssl.SNIHostName
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object CertScan {

  fun scan(
      host: String,
      port: Int = 443,
      sni: String? = null,
      timeout: Duration = 2.seconds
  ): List<X509Certificate> {
    val trustManager = SavingTrustManager()
    val socket = trustManager.newTLSSocket()
    return socket.use { sock ->
      val handshake = runCatching {
        sni?.let {
          // sock.sslParameters will create a new copy
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
