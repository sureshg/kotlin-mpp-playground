package dev.suresh

import com.github.marschall.directorykeystore.*
import java.nio.file.Path
import java.security.KeyStore
import java.security.Security

/**
 * JVM can be switched to use a different truststore using **-Djavax.net.ssl.trustStoreType=xxx**
 */
object TrustStore {

  fun allTrustStores(): List<String> =
      Security.getProviders()
          .flatMap { it.entries }
          .map { it.key.toString() }
          .filter { it.startsWith("KeyStore.") && it.endsWith("ImplementedIn").not() }
          .map { it.substringAfter("KeyStore.").trim() }
          .distinct()

  fun systemTrustStore(keystoreType: SystemKeyStoreType): KeyStore =
      KeyStore.getInstance(keystoreType.type).apply { load(null, null) }

  fun systemTrustStore(path: Path): KeyStore {
    Security.addProvider(DirectoryKeystoreProvider())
    return KeyStore.getInstance("directory").apply { load(DirectoryLoadStoreParameter(path)) }
  }
}

enum class SystemKeyStoreType(val type: String) {
  WIN_USER("Windows-MY"),
  WIN_SYSTEM("Windows-ROOT"),
  MACOS_USER("KeychainStore"),
  MACOS_SYSTEM("KeychainStore-ROOT")
}
