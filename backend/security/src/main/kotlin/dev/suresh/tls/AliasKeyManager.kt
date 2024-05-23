package dev.suresh.tls

import java.net.Socket
import java.security.Principal
import javax.net.ssl.X509KeyManager

/**
 * A [X509KeyManager] implementation which selects the client private key for client authentication
 * based on given key alias name.
 */
class AliasKeyManager(private val delegate: X509KeyManager, private val aliasName: String) :
    X509KeyManager by delegate {
  override fun chooseClientAlias(
      keyType: Array<String>,
      issuers: Array<Principal>,
      socket: Socket
  ): String = aliasName
}
