import kotlin.time.Instant
import kotlinx.cinterop.*
import kotlinx.io.files.Path
import platform.posix.*

actual fun readPassword(prompt: String) = getpass(prompt)?.toKString()

fun getMTime(path: Path): Instant {
  memScoped {
    val stat = alloc<stat>()
    if (lstat(path.toString(), stat.ptr) != 0) {
      throw IllegalStateException("Failed to get mtime for $path")
    }
    return Instant.fromEpochSeconds(stat.st_mtim.tv_sec, stat.st_mtim.tv_nsec)
  }
}
