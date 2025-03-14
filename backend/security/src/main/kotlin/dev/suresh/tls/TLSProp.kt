package dev.suresh.tls

import java.net.JarURLConnection
import java.security.Security
import java.util.jar.Manifest
import kotlin.reflect.KClass

/**
 * Enumeration of JSSE (Java Secure Socket Extension) system and security properties used for
 * configuring TLS/SSL connections.
 */
enum class TLSProp(val prop: String, val desc: String, val system: Boolean = true) {
  Debug("javax.net.debug", "Debugging SSL/TLS Connections."),
  KeyStore("javax.net.ssl.keyStore", "Default keystore"),
  KeyStoreType("javax.net.ssl.keyStoreType", "Default keystore type"),
  KeyStorePassword("javax.net.ssl.keyStorePassword", "Default keystore password"),
  KeyStoreProvider("javax.net.ssl.keyStoreProvider", "Default keystore provider"),
  TrustStore("javax.net.ssl.trustStore", "Default truststore"),
  TrustStoreType("javax.net.ssl.trustStoreType", "Default truststore type"),
  TrustStorePassword("javax.net.ssl.trustStorePassword", "Default truststore password"),
  TrustStoreProvider("javax.net.ssl.trustStoreProvider", "Default truststore provider"),
  ProxyHost("https.proxyHost", "Default HTTPS proxy host"),
  ProxyPort("https.proxyPort", "Default HTTPS proxy port"),
  HttpsCipherSuites("https.cipherSuites", "Default cipher suites"),
  HttpsProtocols("https.protocols", "Default HTTPS handshaking protocols"),
  TLSProtocols("jdk.tls.client.protocols", "Default Enabled TLS Protocols"),
  CertPathDisabledAlgos(
      "jdk.certpath.disabledAlgorithms",
      "Disabled certificate verification cryptographic algorithms",
      false),
  TLSDisabledAlgos("jdk.tls.disabledAlgorithms", "Disabled/Restricted Algorithms", false);

  /** Sets the JSSE system/security property to the given value. */
  fun set(value: String) {
    when (system) {
      true -> System.setProperty(prop, value)
      else -> Security.setProperty(prop, value)
    }
  }
}

/**
 * Returns the jar [Manifest] of the class. Returns `null` if the class is not bundled in a jar
 * (Classes in an unpacked class hierarchy).
 */
val <T : Any> KClass<T>.jarManifest: Manifest?
  get() {
    val res = java.getResource("${java.simpleName}.class")
    val conn = res?.openConnection()
    return if (conn is JarURLConnection) conn.manifest else null
  }
