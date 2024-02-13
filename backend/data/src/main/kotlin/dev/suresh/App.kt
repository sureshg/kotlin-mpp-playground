package dev.suresh

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.pty4j.PtyProcessBuilder
import java.util.concurrent.CountDownLatch

fun main() {
  println("Hello Kotlin Data! ${Greeting().greeting()}")

  val b1 = BigInteger.parseString("12345678901234567890", 10)
  val b2 = BigInteger.parseString("323456789012345678901", 10)
  val s = b1 + b2

  println(b1.bitLength())
  println(b2.bitLength())
  println(s)
  println(s.bitLength())
  ssh()
}

fun ssh() {
  val ptyProcess =
      PtyProcessBuilder().run {
        setCommand(arrayOf("/bin/sh", "-l"))
        setEnvironment(mapOf("TERM" to "xterm-256color"))
        setDirectory(System.getProperty("user.home"))
        start()
      }

  println("PTY child process started ${ptyProcess.pid()}, size: ${ptyProcess.winSize}")
  println(ptyProcess.isConsoleMode)
  println(ptyProcess.isAlive)
  val tos = ptyProcess.outputStream
  val tis = ptyProcess.inputReader()

  val size = 10
  val latch = CountDownLatch(size)

  Thread.ofVirtual().start {
    runCatching {
      for (i in 1..size) {
        tos.write("echo $i".encodeToByteArray())
        tos.write(byteArrayOf(ptyProcess.enterKeyCode))
        tos.flush()
        Thread.sleep(100)
        latch.countDown()
      }
    }
  }

  Thread.ofVirtual().start { runCatching { tis.forEachLine { println(it) } } }

  latch.await()
  tos.close()
  tis.close()

  // wait until the PTY child process is terminated
  val result = ptyProcess.waitFor()
  println("PTY child process terminated with exit code $result")
  ptyProcess.destroyForcibly()
}

// fun wasm() {
//  val source =
//      Source.newBuilder(
//              "wasm",
//              """
//              (module
//                (func (export "add") (param i32 i32) (result i32)
//                  local.get 0
//                  local.get 1
//                  i32.add)
//              )
//              """
//                  .trimIndent(),
//              "test")
//          .build()
//  Context.newBuilder("wasm").build().use { ctx ->
//    val wasm = ctx.eval(source)
//    val add = wasm.getMember("add")
//    val result = add.execute(10, 20)
//    println(result)
//  }
// }
