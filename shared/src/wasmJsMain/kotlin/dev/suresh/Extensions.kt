package dev.suresh

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray

/** JavaScript console class */
external class Console : JsAny {
  fun log(message: String?)

  fun log(message: JsAny?)
}

/** JavaScript console object */
external val console: Console

fun currentTimeMillis(): Long = currentTimeMillisJs().toLong()

private fun currentTimeMillisJs(): Double = js("new Date().getTime()")

fun ArrayBuffer?.toByteArray() = this?.run { Int8Array(this).toByteArray() }
