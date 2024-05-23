package dev.suresh.tls

import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class CustomSSLSocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {
  override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

  override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

  override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket =
      delegate.createSocket(s, host, port, autoClose).apply { reConfigure() }

  override fun createSocket(host: String, port: Int): Socket =
      delegate.createSocket(host, port).apply { reConfigure() }

  override fun createSocket(
      host: String,
      port: Int,
      localHost: InetAddress,
      localPort: Int
  ): Socket = delegate.createSocket(host, port, localHost, localPort).apply { reConfigure() }

  override fun createSocket(host: InetAddress, port: Int): Socket =
      delegate.createSocket(host, port).apply { reConfigure() }

  override fun createSocket(
      address: InetAddress,
      port: Int,
      localAddress: InetAddress,
      localPort: Int
  ): Socket = delegate.createSocket(address, port, localAddress, localPort).apply { reConfigure() }
}

fun Socket.reConfigure() {
  val sslSock = this as SSLSocket
  val sslParams = sslSock.sslParameters
  sslParams.serverNames = emptyList()
  sslParameters = sslParams
}
