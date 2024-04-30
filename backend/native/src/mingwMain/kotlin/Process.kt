import kotlinx.cinterop.*
import platform.windows.*

actual fun execute(command: String, vararg args: String): ProcessResult = memScoped {
  val cmd = (listOf(command) + args).joinToString(" ")

  val si = alloc<STARTUPINFO>()
  val pi = alloc<PROCESS_INFORMATION>()

  val readPipe = alloc<HANDLEVar>()
  val writePipe = alloc<HANDLEVar>()
  val sa = alloc<SECURITY_ATTRIBUTES>()
  sa.nLength = sizeOf<SECURITY_ATTRIBUTES>().toUInt()
  sa.bInheritHandle = 1

  if (CreatePipe(readPipe.ptr, writePipe.ptr, sa.ptr, 0u) == 0) {
    error("CreatePipe failed")
  }

  si.cb = sizeOf<STARTUPINFO>().toUInt()
  si.hStdOutput = writePipe.value
  si.hStdError = writePipe.value
  si.dwFlags = STARTF_USESTDHANDLES.toUInt()

  if (CreateProcess?.let {
    it(null, cmd.wcstr.ptr, null, null, 1, 0u, null, null, si.ptr, pi.ptr)
  } == 0) {
    error("CreateProcess failed")
  }

  CloseHandle(writePipe.value)

  val buffer = allocArray<ByteVar>(4096)
  val bytesRead = alloc<DWORDVar>()
  var output = ""

  while (true) {
    if (ReadFile(readPipe.value, buffer, 4096u, bytesRead.ptr, null) == 0) break
    output += buffer.readBytes(bytesRead.value.toInt()).decodeToString()
  }

  WaitForSingleObject(pi.hProcess, INFINITE)

  val exitCode = alloc<DWORDVar>()
  GetExitCodeProcess(pi.hProcess, exitCode.ptr)

  CloseHandle(pi.hProcess)
  CloseHandle(pi.hThread)
  CloseHandle(readPipe.value)

  ProcessResult(exitCode.value.toInt(), output)
}
