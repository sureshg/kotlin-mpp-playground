@file:OptIn(DelicateCoroutinesApi::class)

package dev.suresh

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.awaitClosed
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import java.net.InetSocketAddress
import java.time.Duration
import kotlin.concurrent.thread
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals

class KtorTests {

  // @Test
  fun tcpServer() = runTest {
    val address = InetSocketAddress.createUnresolved("localhost", 9999)
    val selectorManager = SelectorManager(newSingleThreadContext("TcpRunner"))

    aSocket(selectorManager).tcp().bind(address.hostName, address.port).use { serverSocket ->
      println("TCP server is listening at ${serverSocket.localAddress}")

      repeat(5_000) {
        // If the thread hangs, this won't complete within the timeout
        println("Awaiting new connection")
        val socketAsync = async { serverSocket.accept() }
        aSocket(selectorManager).tcp().connect(address.hostName, address.port)
        val socket = socketAsync.await()
        println("Accepted connection from ${socket.remoteAddress}, $socket")

        println("Closing connection to ${socket.remoteAddress}")

        val thread = thread {
          val readChannel = socket.openReadChannel()
          val writeChannel = socket.openWriteChannel(autoFlush = true)

          socket.close()
          runBlocking {
            // With manual flushAndClose of the WriteChannel, 3.2.0 works.
            //
            // Note: This is _usually_ not needed, only a small percentage of connections
            //       fail to close properly without it.
            //                                writeChannel.flushAndClose()
            socket.awaitClosed()
          }
        }

        thread.join(Duration.ofMillis(500))
        assertEquals(Thread.State.TERMINATED, thread.state)
      }
    }
  }
}
