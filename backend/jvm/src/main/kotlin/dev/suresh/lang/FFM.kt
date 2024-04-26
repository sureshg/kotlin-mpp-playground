package dev.suresh.lang

import dev.suresh.*
import io.github.oshai.kotlinlogging.KLogger
import java.lang.foreign.*
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.time.Instant

object FFM {

  context(KLogger)
  suspend fun memoryLayout() = runOnVirtualThread {
    memoryAPIs()
    currTime()
    strlen("Hello Panama!")
    getPid()
    gmtime()
    terminal()
    dhReflection()
  }

  context(KLogger)
  private fun strlen(str: String) {
    val strlenAddr = SYMBOL_LOOKUP.findOrNull("strlen")
    val strlenDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, AddressLayout.ADDRESS)
    val strlen = LINKER.downcallHandle(strlenAddr, strlenDescriptor)
    Arena.ofConfined().use { arena ->
      val cString = arena.allocateFrom(str)
      val strlenResult = strlen.invokeExact(cString) as Int
      info { """strlen("$str") = $strlenResult""" }
    }
  }

  context(KLogger)
  private fun currTime() {
    // Print the current time.
    val timeAddr = SYMBOL_LOOKUP.findOrNull("time")
    val timeDesc = FunctionDescriptor.of(ValueLayout.JAVA_LONG)
    val time = LINKER.downcallHandle(timeAddr, timeDesc)
    val timeResult = time.invokeExact() as Long
    info { "time() = $timeResult epochSecond" }
  }

  context(KLogger)
  private fun gmtime() {
    val gmtAddr = SYMBOL_LOOKUP.findOrNull("gmtime")
    val gmtDesc =
        FunctionDescriptor.of(
            AddressLayout.ADDRESS.withTargetLayout(TM.LAYOUT), AddressLayout.ADDRESS)
    val gmtime = LINKER.downcallHandle(gmtAddr, gmtDesc)

    Arena.ofConfined().use { arena ->
      val time = arena.allocate(ValueLayout.JAVA_LONG.byteSize())
      time.set(ValueLayout.JAVA_LONG, 0, Instant.now().epochSecond)
      val tmSegment = gmtime.invokeExact(time) as MemorySegment
      info { "gmtime() = ${TM(tmSegment)}" }
    }
  }

  context(KLogger)
  private fun getPid() {
    val getpidAddr = SYMBOL_LOOKUP.findOrNull("getpid")
    val getpidDesc = FunctionDescriptor.of(ValueLayout.JAVA_INT)
    val getpid = LINKER.downcallHandle(getpidAddr, getpidDesc)
    val pid = getpid.invokeExact() as Int
    assert(pid.toLong() == ProcessHandle.current().pid())
    info { "getpid() = $pid" }
  }

  /**
   * Allocate memory for two doubles and initialize it.
   *
   * ```c
   *  Struct Point2D {
   *    double x;
   *    double y;
   *  } point = { 1.0, 2.0 };
   * ```
   */
  context(KLogger)
  private fun memoryAPIs() {
    Arena.ofConfined().use { arena ->
      val point = arena.allocate(ValueLayout.JAVA_DOUBLE.byteSize() * 2)
      point.set(ValueLayout.JAVA_DOUBLE, 0, 1.0)
      point.set(ValueLayout.JAVA_DOUBLE, 8, 2.0)
      info { "Point Struct = $point" }
      info {
        """Struct {
          |  x = ${point.get(ValueLayout.JAVA_DOUBLE, 0)} ,
          |  y = ${point.get(ValueLayout.JAVA_DOUBLE, 8)}
          |}
         """
            .trimMargin()
      }
    }

    val point2D =
        MemoryLayout.structLayout(
                ValueLayout.JAVA_DOUBLE.withName("x"),
                ValueLayout.JAVA_DOUBLE.withName("y"),
            )
            .withName("Point2D")

    // VarHandle accessors for the struct fields
    val x = point2D.varHandle(MemoryLayout.PathElement.groupElement("x"))
    val y = point2D.varHandle(MemoryLayout.PathElement.groupElement("y"))

    Arena.ofConfined().use { arena ->
      // val seg = MemorySegment.allocateNative(8,arena.scope())
      val point = arena.allocate(point2D)
      x.set(point, 0L, 1.0)
      y.set(point, 0L, 2.0)
      info { "Point2D segment = $point" }
      info {
        """Point2D {
          |  x = ${x.get(point, 0L)} ,
          |  y = ${y.get(point, 0L)}
          |}"""
            .trimMargin()
      }
    }

    // Allocate an off-heap region of memory big enough to hold 10 values of the primitive type int,
    // and fill it with values ranging from 0 to 9
    Arena.ofConfined().use { arena ->
      val count = 10
      val segment = arena.allocate(count * ValueLayout.JAVA_INT.byteSize())
      for (i in 0..<count) {
        segment.setAtIndex(ValueLayout.JAVA_INT, i.toLong(), i)
      }
    }
  }

  context(KLogger)
  private fun terminal() {
    val winsize =
        MemoryLayout.structLayout(
                ValueLayout.JAVA_SHORT.withName("ws_row"),
                ValueLayout.JAVA_SHORT.withName("ws_col"),
                ValueLayout.JAVA_SHORT.withName("ws_xpixel"),
                ValueLayout.JAVA_SHORT.withName("ws_ypixel"),
            )
            .withName("winsize")

    val wsRow = winsize.varHandle(MemoryLayout.PathElement.groupElement("ws_row"))
    val wsCol = winsize.varHandle(MemoryLayout.PathElement.groupElement("ws_col"))
    val wsXpixel = winsize.varHandle(MemoryLayout.PathElement.groupElement("ws_xpixel"))
    val wsYpixel = winsize.varHandle(MemoryLayout.PathElement.groupElement("ws_ypixel"))

    val ioctlFun =
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.JAVA_INT,
            ValueLayout.JAVA_LONG,
            AddressLayout.ADDRESS.withTargetLayout(winsize))
    val isAttyFun = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)

    // For capturing the errno value
    val ccs = Linker.Option.captureCallState("errno")
    val csLayout = Linker.Option.captureStateLayout()
    val errnoHandle = csLayout?.varHandle(MemoryLayout.PathElement.groupElement("errno"))

    val ioctl = downcallHandle("ioctl", ioctlFun, ccs, Linker.Option.firstVariadicArg(2))
    val isAtty = downcallHandle("isatty", isAttyFun)

    Arena.ofConfined().use { arena ->
      val isTerminal = isAtty?.invokeExact(1) as Int != 0
      if (isTerminal) {
        val winSeg = arena.allocate(winsize)
        val capturedState = arena.allocate(csLayout)
        val winRet = ioctl?.invokeExact(capturedState, 1, 0x40087468L, winSeg) as Int

        if (winRet == -1) {
          val errno = errnoHandle?.get(capturedState, 0L) as Int
          info { "ioctl() errno: $errno" }
        } else {
          info {
            """
                |winsize {
                |  ws_row = ${wsRow.get(winSeg, 0L)}
                |  ws_col = ${wsCol.get(winSeg, 0L)}
                |  ws_xpixel = ${wsXpixel.get(winSeg, 0L)}
                |  ws_ypixel = ${wsYpixel.get(winSeg, 0L)}
                |}
                """
                .trimMargin()
          }
        }
      } else {
        info { "Not a TTY" }
      }
    }

    /** Atomic access for long[] using a MemorySegment */
    fun arrayAtomicAccess() {
      val arr = LongArray(10)
      val arrSeg = MemorySegment.ofArray(arr)
      val vh = ValueLayout.JAVA_INT.varHandle()
      vh.setVolatile(arrSeg, 0L, 69)
      assert(vh.getVolatile(arrSeg, 0L) as Int == 69)
    }
  }

  /** Reflectively invoke the downcallHandle method on the Linker class. */
  context(KLogger)
  private fun dhReflection() {
    val mh =
        MethodHandles.lookup()
            .findVirtual(
                Linker::class.java,
                "downcallHandle",
                MethodType.methodType(
                    MethodHandle::class.java,
                    FunctionDescriptor::class.java,
                    Array<Linker.Option>::class.java,
                ),
            )
    val handle =
        mh.invokeExact(LINKER, FunctionDescriptor.ofVoid(), arrayOf<Linker.Option>())
            as MethodHandle
    info { "Got downcall handle: $handle" }
  }
}
