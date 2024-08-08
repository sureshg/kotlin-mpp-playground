package dev.suresh.cert

import dev.suresh.log
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.CertPathValidator
import java.security.cert.PKIXParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import javax.naming.ldap.LdapName
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime

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
              PemFormat.ALT_RFC822_NAME,
              PemFormat.ALT_DNS_NAME,
              PemFormat.ALT_IPA_NAME -> it[1].toString()
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
  get() = keyUsage?.get(5) ?: false

/** Returns `true` if the certificate is an intermediate CA certificate, `false` otherwise. */
val X509Certificate.isIntermediateCA
  get() = isCA && !selfSigned

/** Returns `true` if the certificate is any of the custom Root CAs */
val X509Certificate.isRootCA
  get() = isCA && commonName in RootCA.commonNames

val X509Certificate.isISRGRootX1
  get() = isCA && commonName == RootCA.ISRG_ROOT_X1

val X509Certificate.isISRGRootX2
  get() = isCA && commonName == RootCA.ISRG_ROOT_X2

/** Returns the certificate expiry date time in UTC. */
val X509Certificate.expiryDateUTC
  get() = notAfter.toInstant().toKotlinInstant().toLocalDateTime(TimeZone.UTC)

val PublicKey.pem: String
  get() = PemFormat.encodePem("PUBLIC KEY", encoded)

val PrivateKey.pem: String
  get() = PemFormat.encodePem("PRIVATE KEY", encoded)

val X509Certificate.pem: String
  get() = PemFormat.encodePem("CERTIFICATE", encoded)

/** Returns true if the given cert chain is signed by custom Root CAs. */
val List<X509Certificate>.isSignedByRootCA
  get(): Boolean {
    check(isNotEmpty()) { "Cert chain is empty" }
    val customTrustAnchors = RootCA.certs.map { TrustAnchor(it, null) }.toSet()
    val params = PKIXParameters(customTrustAnchors).apply { isRevocationEnabled = false }

    val certPath = PemFormat.certFactory.generateCertPath(this)
    val certPathValidator = CertPathValidator.getInstance("PKIX")
    return runCatching { certPathValidator.validate(certPath, params) }
        .onFailure { log.error(it) { "'${first().commonName}' cert chain validity check failed!" } }
        .isSuccess
  }
