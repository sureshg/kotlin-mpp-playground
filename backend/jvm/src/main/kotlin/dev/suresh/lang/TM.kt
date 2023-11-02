package dev.suresh.lang

import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Time representing the number of seconds since the Unix Epoch (January 1, 1970, 00:00:00 UTC) into
 * a broken-down UTC representation
 */
class TM(private val segment: MemorySegment) {
  companion object {
    val LAYOUT =
        MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("sec"),
            ValueLayout.JAVA_INT.withName("min"),
            ValueLayout.JAVA_INT.withName("hour"),
            ValueLayout.JAVA_INT.withName("mday"),
            ValueLayout.JAVA_INT.withName("mon"),
            ValueLayout.JAVA_INT.withName("year"),
            ValueLayout.JAVA_INT.withName("wday"),
            ValueLayout.JAVA_INT.withName("yday"),
            ValueLayout.JAVA_BOOLEAN.withName("isdst"),
            MemoryLayout.paddingLayout(24))

    private val yearVH = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("year"))
    private val monthVH = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("mon"))
    private val dayVH = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("mday"))
    private val hourVH = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("hour"))
    private val minVH = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("min"))
    private val secVH = LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("sec"))
  }

  val year: Int
    get() = yearVH.get(segment, 0L) as Int + 1900

  val month: Int
    get() = monthVH.get(segment, 0L) as Int + 1

  val day: Int
    get() = dayVH.get(segment, 0L) as Int

  val hour: Int
    get() = hourVH.get(segment, 0L) as Int

  val min: Int
    get() = minVH.get(segment, 0L) as Int

  val sec: Int
    get() = secVH.get(segment, 0L) as Int

  override fun toString(): String {
    return "TM(year=$year, month=$month, day=$day, hour=$hour, min=$min, sec=$sec)"
  }
}
