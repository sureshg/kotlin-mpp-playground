package dev.suresh.cert

/** Custom TrustAnchors */
object RootCA {

  const val ISRG_ROOT_X1 = "ISRG Root X1"

  const val ISRG_ROOT_X2 = "ISRG Root X2"

  val certs by lazy {
    val pem =
        ClassLoader.getSystemResource("ca/cacert.pem")?.readText(Charsets.US_ASCII)
            ?: error("RootCAs (ca/cacert.pem) not found!")
    PemFormat.readCertChain(pem).onEach { it.checkValidity() }
  }

  val commonNames
    get() = certs.map { it.commonName }

  val isrgRootX1
    get() = certs.first { it.commonName == ISRG_ROOT_X1 }

  val isrgRootX2
    get() = certs.first { it.commonName == ISRG_ROOT_X2 }
}
