import kotlin.coroutines.suspendCoroutine
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
          val files = input.files?.asList().orEmpty()
          cont.resumeWith(Result.success(files))
        }
      }
      body?.appendChild(input)
      input.click()
      input.remove()
    }

suspend fun File.readAsText(): String = suspendCoroutine { cont ->
  val reader = FileReader()
  reader.onload = {
    val result = it.target.toString()
    cont.resumeWith(Result.success(result))
  }

  reader.onprogress = {
    val percentage = (it.loaded.toDouble() / it.total.toDouble()) * 100
    log.info { "File loading Progress: $percentage%" }
  }
  reader.readAsText(this, "UTF-8")
}
