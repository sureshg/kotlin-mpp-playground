package dev.suresh.cert

import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.*
import javax.naming.ldap.LdapName
import kotlin.io.encoding.Base64
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime

const val ALT_RFC822_NAME = 1
const val ALT_DNS_NAME = 2
const val ALT_IPA_NAME = 7

/** PEM certificate pattern */
val CERT_PATTERN =
    """-+BEGIN\s+.*CERTIFICATE[^-]*-+(?:\s|\r|\n)+([a-z0-9+/=\r\n]+)-+END\s+.*CERTIFICATE[^-]*-+"""
        .toRegex(RegexOption.IGNORE_CASE)

val certFactory = CertificateFactory.getInstance("X.509")

/** Returns the certificate subject DN (common name) from the certificate */
val X509Certificate.commonName
  get() =
      LdapName(subjectX500Principal.name)
          .rdns
          .filter { it.type.equals("CN", true) }
          .map { it.value.toString() }
          .single()

/** Returns the subject alternative names from the certificate. */
val X509Certificate.subjectAltNames
  get() =
      subjectAlternativeNames
          .orEmpty()
          .filter { it.size == 2 }
          .mapNotNull {
            val oid = it[0].toString().toInt()
            when (oid) {
              ALT_RFC822_NAME,
              ALT_DNS_NAME,
              ALT_IPA_NAME -> it[1].toString()
              else -> null
            }
          }

/**
 * Returns true if the certificate is signed by the given [ca] cert., false otherwise.
 *
 * @param ca [X509Certificate] CA cert.
 */
fun X509Certificate.signedBy(ca: X509Certificate): Boolean =
    when {
      issuerX500Principal != ca.subjectX500Principal -> false
      else -> runCatching { verify(ca.publicKey) }.isSuccess
    }

/** Returns true if the certificate is self-signed, false otherwise. */
val X509Certificate.selfSigned
  get() = signedBy(this)

/**
 * Returns `true` if the certificate is a CA certificate.
 *
 * @see [X509Certificate.getKeyUsage]
 */
val X509Certificate.isCA
  get() = keyUsage?.get(5) == true

/** Returns `true` if the certificate is an intermediate CA certificate, `false` otherwise. */
val X509Certificate.isIntermediateCA
  get() = isCA && !selfSigned

/** Returns the certificate expiry date time in UTC. */
val X509Certificate.expiryDateUTC
  get() = notAfter.toInstant().toKotlinInstant().toLocalDateTime(TimeZone.UTC)

val PublicKey.pem: String
  get() = encodePem("PUBLIC KEY", encoded)

val PrivateKey.pem: String
  get() = encodePem("PRIVATE KEY", encoded)

val X509Certificate.pem: String
  get() = encodePem("CERTIFICATE", encoded)

/** Returns true if the cert chain is signed by given Root CA */
fun List<X509Certificate>.isSignedByRoot(root: List<X509Certificate>): Boolean {
  check(isNotEmpty()) { "Cert chain is empty" }
  val trustAnchors =
      root
          .map {
            // it.checkValidity()
            TrustAnchor(it, null)
          }
          .toSet()
  val params = PKIXParameters(trustAnchors).apply { isRevocationEnabled = false }
  val certPath = certFactory.generateCertPath(this)
  val certPathValidator = CertPathValidator.getInstance("PKIX")
  return runCatching { certPathValidator.validate(certPath, params) }.isSuccess
}

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
