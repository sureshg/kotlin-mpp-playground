package dev.suresh.cert

import java.security.KeyFactory
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.Cipher.DECRYPT_MODE
import javax.crypto.EncryptedPrivateKeyInfo
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
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

  /** PEM private key pattern */
  private val KEY_PATTERN =
      """-+BEGIN\s+(?:(.*)\s+)?PRIVATE\s+KEY[^-]*-+(?:\s|\r|\n)+([a-z0-9+/=\r\n]+)-+END\s+.*PRIVATE\s+KEY[^-]*-+"""
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

  /** Read PKCS#8 encoded private key. */
  fun readPrivateKey(privateKey: String, password: String? = null) =
      KEY_PATTERN.findAll(privateKey)
          .map {
            val keyType = it.groupValues[1]
            val base64Key = it.groupValues[2]
            if (base64Key.lowercase(locale = Locale.US).startsWith("proc-type")) {
              throw InvalidKeySpecException(
                  "Password protected PKCS1 private keys are not supported"
              )
            }

            val encodedKey = Base64.Mime.decode(base64Key.toByteArray(Charsets.US_ASCII))
            val encKeySpec =
                when (keyType) {
                  "ENCRYPTED" -> {
                    require(password != null) {
                      "Private key is encrypted, but no password was provided"
                    }
                    val encPrivateKeyInfo = EncryptedPrivateKeyInfo(encodedKey)
                    val keyFactory = SecretKeyFactory.getInstance(encPrivateKeyInfo.algName)
                    val secretKey = keyFactory.generateSecret(PBEKeySpec(password.toCharArray()))

                    val cipher = Cipher.getInstance(encPrivateKeyInfo.algName)
                    cipher.init(DECRYPT_MODE, secretKey, encPrivateKeyInfo.algParameters)
                    encPrivateKeyInfo.getKeySpec(cipher)
                  }
                  else -> PKCS8EncodedKeySpec(encodedKey)
                }

            KeyFactory.getInstance("RSA").generatePrivate(encKeySpec)
          }
          .toList()

  /** Encodes the given [encoded] bytes to PEM format. */
  fun encodePem(pemString: PemString, encoded: ByteArray) =
      """-----BEGIN ${pemString.beginMarker}-----
      |${Base64.encode(encoded).chunked(64).joinToString("\n")}
      |-----END ${pemString.beginMarker}-----
      |"""
          .trimMargin()
}

/**
 * PEM string begin markers.
 *
 * [Pem.h](https://github.com/openssl/openssl/blob/master/include/openssl/pem.h#L35)
 */
enum class PemString(val beginMarker: String) {
  X509("CERTIFICATE"),
  X509_REQ("CERTIFICATE REQUEST"),
  X509_CRL("X509 CRL"),
  PUBLIC("PUBLIC KEY"),
  // Openssl -traditional
  PKCS1("RSA PRIVATE KEY"),
  RSA_PUBLIC("RSA PUBLIC KEY"),
  DSA("DSA PRIVATE KEY"),
  DSA_PUBLIC("DSA PUBLIC KEY"),
  PKCS8("ENCRYPTED PRIVATE KEY"),
  // Unencrypted PKCS#8 private key
  PKCS8INF("PRIVATE KEY"),
  ECDSA_PUBLIC("ECDSA PUBLIC KEY"),
  EC_PARAMETERS("EC PARAMETERS"),
  EC_PRIVATE_KEY("EC PRIVATE KEY"),
}
