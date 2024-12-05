import kotlin.test.assertEquals
import kotlinx.io.Buffer
import kotlinx.io.files.*
import kotlinx.io.readFloat

fun buffer() {
  val buffer = Buffer()
  buffer.write(byteArrayOf(70, 64, -26, -74))
  assertEquals(12345.678F.toBits(), buffer.readFloat().toBits())
}

fun dir() {
  println("SystemPathSeparator = $SystemPathSeparator")
  println("SystemTemporaryDirectory: $SystemTemporaryDirectory")
  SystemFileSystem.list(Path(".")).forEach { println(it) }
}
