import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

/** JavaScript console class */
external class Console : JsAny {
  fun log(message: String?)

  fun log(message: JsAny?)
}

/** JavaScript console object */
external val console: Console

fun currentTimeMillis(): Long = currentTimeMillisJs().toLong()

private fun currentTimeMillisJs(): Double = js("new Date().getTime()")

fun Int8Array.copyInto(
    output: ByteArray,
    inputOffset: Int = 0,
    outputOffset: Int = 0,
    length: Int = this.length
) = repeat(length) { output[outputOffset + it] = this[inputOffset + it] }

fun Int8Array.toByteArray(): ByteArray {
  val buf = ByteArray(length)
  copyInto(buf)
  return buf
}

fun ByteArray.copyInto(
    output: Int8Array,
    inputOffset: Int = 0,
    outputOffset: Int = 0,
    length: Int = this.size
) = repeat(length) { output[outputOffset + it] = this[inputOffset + it] }

fun ByteArray.toInt8Array(): Int8Array {
  val arr = Int8Array(size)
  copyInto(arr)
  return arr
}

fun ArrayBuffer?.toByteArray() = this?.run { Int8Array(this).toByteArray() }

fun ByteArray.toArrayBuffer() = toInt8Array().buffer
