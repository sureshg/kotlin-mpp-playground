package dev.suresh

import kotlin.native.Platform as KNPlatform
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.posix.fgets
import platform.posix.getenv
import platform.posix.pclose
import platform.posix.popen

actual val platform: Platform = NativePlatform

object NativePlatform : Platform {
  override val name: String = "Native"

  override fun env(key: String, def: String?) = getenv(key)?.toKString() ?: def

  override val osInfo: Map<String, String?>
    get() =
        super.osInfo +
            super.osInfo +
            mapOf(
                "name" to KNPlatform.osFamily.name,
                "version" to env("OSTYPE", "n/a"),
                "arch" to KNPlatform.cpuArchitecture.name,
                "user" to env("USER"),
            )
}

data class ProcessResult(val code: Int, val rawOutput: String?)

fun execute(command: String, vararg args: String): ProcessResult = memScoped {
  val cmd =
      buildList {
            add(command)
            addAll(args)
          }
          .joinToString(separator = " ") { it.replace(" ", "\\ ") }

  val buffer = ByteArray(128)
  val exitCode: Int
  var result = ""
  val pipe =
      popen(
          "$cmd 2>&1",
          "r") // write stderr together with stdout: https://stackoverflow.com/a/44680326
      ?: error("popen('$cmd 2>&1', 'r') error")

  try {
    while (true) {
      val input = fgets(buffer.refTo(0), buffer.size, pipe) ?: break
      val inputString = input.toKString()
      result += inputString
    }
  } finally {
    exitCode =
        pclose(pipe) /
            256 // get error code from a child process: https://stackoverflow.com/a/808995
  }

  val rawOutput = result.trim().takeIf { it.isNotBlank() }
  ProcessResult(exitCode, rawOutput)
}
