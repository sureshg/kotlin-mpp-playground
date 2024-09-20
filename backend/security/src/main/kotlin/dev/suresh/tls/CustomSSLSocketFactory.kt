package dev.suresh.tls

import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class CustomSSLSocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {
  override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

  override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

  override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket =
      delegate.createSocket(s, host, port, autoClose).apply { reconfigureSSLSocket() }

  override fun createSocket(host: String, port: Int): Socket =
      delegate.createSocket(host, port).apply { reconfigureSSLSocket() }

  override fun createSocket(
      host: String,
      port: Int,
      localHost: InetAddress,
      localPort: Int
  ): Socket =
      delegate.createSocket(host, port, localHost, localPort).apply { reconfigureSSLSocket() }

  override fun createSocket(host: InetAddress, port: Int): Socket =
      delegate.createSocket(host, port).apply { reconfigureSSLSocket() }

  override fun createSocket(
      address: InetAddress,
      port: Int,
      localAddress: InetAddress,
      localPort: Int
  ): Socket =
      delegate.createSocket(address, port, localAddress, localPort).apply { reconfigureSSLSocket() }
}

fun Socket.reconfigureSSLSocket() {
  val sslSock = this as SSLSocket
  val sslParams = sslSock.sslParameters
  // Disable SNI
  sslParams.serverNames = emptyList()
  // Disable Hostname verification (Same as -Djdk.internal.httpclient.disableHostnameVerification)
  sslParams.endpointIdentificationAlgorithm = null
  sslParameters = sslParams
}
