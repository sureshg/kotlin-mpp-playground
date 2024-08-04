package dev.suresh

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.Document
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import org.w3c.files.File
import org.w3c.files.FileReader

/**
 * The code is adapted from
 * [compose-multiplatform-file-picker](https://github.com/Wavesonics/compose-multiplatform-file-picker)
 *
 * [Read Files](https://web.dev/articles/read-files)
 */
suspend fun Document.selectFileFromDisk(accept: String? = null, multiple: Boolean = false) =
    suspendCoroutine { cont ->
      val input = createElement("input") as HTMLInputElement
      input.apply {
        type = "file"
        style.display = "none"
        this.multiple = multiple
        accept?.let { this.accept = it }
        onchange = {
          try {
            val files = input.files?.asList().orEmpty()
            cont.resume(files)
          } catch (e: Exception) {
            cont.resumeWithException(e)
          }
        }
      }
      body?.appendChild(input)
      input.click()
      input.remove()
    }

suspend fun File.readAsText() = suspendCoroutine { cont ->
  val reader = FileReader()
  reader.onload = {
    try {
      val target = it.target as? FileReader
      val result = target?.result?.unsafeCast<JsString>()
      cont.resume(result?.toString())
    } catch (e: Exception) {
      cont.resumeWithException(e)
    }
  }

  reader.onerror = {
    cont.resumeWithException(IllegalStateException("Error reading the file '$name' as text!"))
  }

  reader.onprogress = {
    val percentage = (it.loaded.toDouble() / it.total.toDouble()) * 100
    log.info { "File loading Progress: $percentage%" }
  }
  reader.readAsText(this, "UTF-8")
}

suspend fun File.readAsByteArray() = suspendCoroutine { cont ->
  val reader = FileReader()
  reader.onload = {
    try {
      val target = it.target as? FileReader
      val result = target?.result?.unsafeCast<ArrayBuffer>()
      cont.resume(result.toByteArray())
    } catch (e: Exception) {
      cont.resumeWithException(e)
    }
  }

  reader.onerror = {
    cont.resumeWithException(IllegalStateException("Error reading the file '$name' as byte array!"))
  }

  reader.onprogress = {
    val percentage = (it.loaded.toDouble() / it.total.toDouble()) * 100
    log.info { "File loading Progress: $percentage%" }
  }
  reader.readAsArrayBuffer(this)
}
