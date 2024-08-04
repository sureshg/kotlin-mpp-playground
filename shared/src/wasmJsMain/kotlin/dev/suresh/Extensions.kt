package dev.suresh

import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.withScopedMemoryAllocator
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array

/** JavaScript console class */
external class Console : JsAny {
  fun log(message: String?)

  fun log(message: JsAny?)
}

/** JavaScript console object */
external val console: Console

fun currentTimeMillis(): Long = currentTimeMillisJs().toLong()

private fun currentTimeMillisJs(): Double = js("new Date().getTime()")

fun ArrayBuffer?.toByteArray() =
    this?.run {
      val source = Int8Array(this, 0, byteLength)
      jsInt8ArrayToKotlinByteArray(source)
    }

@JsFun(
    """ (src, size, dstAddr) => {
          const mem8 = new Int8Array(wasmExports.memory.buffer, dstAddr, size);
          mem8.set(src);
         }
         """)
internal external fun jsExportInt8ArrayToWasm(src: Int8Array, size: Int, dstAddr: Int)

internal fun jsInt8ArrayToKotlinByteArray(x: Int8Array): ByteArray {
  val size = x.length

  @OptIn(UnsafeWasmMemoryApi::class)
  return withScopedMemoryAllocator { allocator ->
    val memBuffer = allocator.allocate(size)
    val dstAddress = memBuffer.address.toInt()
    jsExportInt8ArrayToWasm(x, size, dstAddress)
    ByteArray(size) { i -> (memBuffer + i).loadByte() }
  }
}
