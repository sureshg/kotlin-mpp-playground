import kotlinx.cinterop.toKString
import platform.posix.getpass

actual fun readPassword(prompt: String) = getpass(prompt)?.toKString()
