package dev.suresh.cert

import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.io.encoding.Base64

/**
 * PEM X.509 certificates reader & writer
 *
 * @author Suresh
 */
object PemFormat {

  /** Supported X.509 SAN OIDs */
  const val ALT_RFC822_NAME = 1
  const val ALT_DNS_NAME = 2
  const val ALT_IPA_NAME = 7

  /** PEM certificate pattern */
  private val CERT_PATTERN =
      """-+BEGIN\s+.*CERTIFICATE[^-]*-+(?:\s|\r|\n)+([a-z0-9+/=\r\n]+)-+END\s+.*CERTIFICATE[^-]*-+"""
          .toRegex(RegexOption.IGNORE_CASE)

  val certFactory = CertificateFactory.getInstance("X.509")

  /**
   * Checks if the given string is a PEM encoded certificate.
   *
   * @param data cert data
   * @return `true` if it's a `PEM` certificate.
   */
  fun isPem(data: String) = CERT_PATTERN.containsMatchIn(data)

  /**
   * Read all X.509 certificates from the given PEM encoded certificate.
   *
   * @param certChain PEM encoded cert(s)
   * @return list of [X509Certificate]
   */
  fun readCertChain(certChain: String) =
      try {
        CERT_PATTERN.findAll(certChain)
            .map {
              val base64Text = it.groupValues[1]
              val buffer = Base64.Mime.decode(base64Text.toByteArray(Charsets.US_ASCII))
              certFactory.generateCertificate(buffer.inputStream()) as X509Certificate
            }
            .toList()
      } catch (e: Exception) {
        throw IllegalStateException("Can't read the PEM certificate, cert data is invalid", e)
      }

  /** Encodes the given [encoded] bytes to PEM format. */
  fun encodePem(type: String, encoded: ByteArray) =
      """-----BEGIN $type-----
      |${Base64.encode(encoded).chunked(64).joinToString("\n")}
      |-----END $type-----
      |"""
          .trimMargin()
}
