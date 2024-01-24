/** JavaScript console class */
external class Console : JsAny {
  fun log(message: String?)

  fun log(message: JsAny?)
}

/** JavaScript console object */
external val console: Console

fun currentTimeMillis(): Long = currentTimeMillisJs().toLong()

private fun currentTimeMillisJs(): Double = js("new Date().getTime()")
