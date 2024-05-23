package dev.suresh.tls

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class SavingTrustManager : X509TrustManager {

  private val _chain = mutableListOf<X509Certificate>()

  val chain: List<X509Certificate>
    get() = _chain

  override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
    _chain.addAll(chain)
  }

  override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
    _chain.addAll(chain)
  }

  override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}
