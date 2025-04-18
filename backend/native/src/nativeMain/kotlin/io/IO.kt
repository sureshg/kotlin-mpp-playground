import kotlin.test.*
import kotlinx.io.*
import kotlinx.io.files.*

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

fun Path.append(data: String) {
  SystemFileSystem.sink(this, append = true).buffered().use { f -> f.writeString(data) }
}
