import kotlinx.cinterop.*
import platform.windows.*

actual fun readPassword(prompt: String): String? = memScoped {
  print(prompt)

  val handle = GetStdHandle(STD_INPUT_HANDLE)
  if (handle == INVALID_HANDLE_VALUE) {
    error("Standard input not available!")
  }

  val record = alloc<INPUT_RECORD>()
  val read = alloc<DWORDVar>()

  buildString {
    while (true) {
      val numberOfRecordsToRead = 1.convert<DWORD>()
      if (ReadConsoleInput?.let { it(handle, record.ptr, numberOfRecordsToRead, read.ptr) } == 0) {
        error("Could not read console input")
      }

      if (record.EventType == KEY_EVENT.toUShort() && record.Event.KeyEvent.bKeyDown != 0) {
        val char = record.Event.KeyEvent.uChar.UnicodeChar
        if (char == '\r'.code.toUShort()) {
          break
        } else if (char != 0.toUShort()) {
          append(char)
        }
      }
    }
  }
}
