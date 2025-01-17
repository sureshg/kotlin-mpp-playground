package dev.suresh

import dev.suresh.http.json
import dev.suresh.lang.*
import dev.suresh.serde.toJsonElement
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.*
import kotlinx.serialization.json.Json

class Greeting {

  fun greeting() = buildString {
    appendLine(json.encodeToString(platform.info))
    appendLine(data())
    appendLine(kotlinxTests())
    appendLine(atomicFUTests())
  }

  private fun data() = buildString {
    val person =
        Person(
            name = Name(first = "Foo", last = "Bar"),
            address =
                Address(
                    street = "123 Main St", city = "San Francisco", state = "CA", zip = "95000"),
            privateInfo = PrivateInfo(ssn = "123-45-6789", dob = "01/01/2000"))
    val modPerson = person.copy() // { address.city = "San Jose" }
    appendLine("Person: $person")
    appendLine("Modified Person: $modPerson")
  }

  private fun atomicFUTests() = buildString {
    val a = AtomicSample()
    appendLine("AtomicFU sample")
    appendLine("Initial value: ${a.x}")
    a.doWork(1234)
    appendLine("Final value: ${a.x}")
    check(a.x == 1234)
    check(a.synchronizedFoo(42) == 42)
    appendLine("Synchronized foo: ${a.synchronizedFoo(42)}")
  }

  private fun kotlinxTests(): String {
    val obj =
        mapOf(
                "bool" to true,
                "byte" to 1.toByte(),
                "uByte" to 1.toUByte(),
                "char" to '2',
                "short" to 3.toShort(),
                "uShort" to 3.toUShort(),
                "int" to 4,
                "uInt" to 4u,
                "long" to 5L,
                "uLong" to 5uL,
                "float" to 6.0f,
                "double" to 7.0,
                "string" to "Kotlin",
                "enum" to YesNo.YES,
                "pair" to ("foo" to "Bar"),
                "triple" to Triple("foo", "bar", "baz"),
                "unit" to Unit,
                "duration" to 2.seconds,
                "uuid" to Uuid.random(),
                "boolArray" to booleanArrayOf(true, false, true),
                "byteArray" to byteArrayOf(1, 2, 3),
                "charArray" to charArrayOf('1', '2', '3'),
                "shortArray" to shortArrayOf(1, 2, 3),
                "intArray" to intArrayOf(1, 2, 3),
                "longArray" to longArrayOf(1, 2, 3),
                "floatArray" to floatArrayOf(1.0f, 1.1f, 1.2f, 1.3f),
                "doubleArray" to doubleArrayOf(1.0, 1.1, 1.2, 1.3),
                "ubyteArray" to ubyteArrayOf(1u, 2u, 3u),
                "ushortArray" to ushortArrayOf(1u, 2u, 3u),
                "uintArray" to uintArrayOf(1u, 2u, 3u),
                "ulongArray" to ulongArrayOf(1u, 2u, 3u),
                "arrayOfInt" to arrayOf(1, 2, 3),
                "arrayOfString" to arrayOf("foo", "bar"),
                "listOfDouble" to listOf(1.1, 2.2, 3.3),
                "listOfString" to listOf("foo", "bar"),
                "setOfString" to setOf("foo", "bar", "baz"),
                "mapOfStringInt" to mapOf("1" to 1, "2" to 2),
            )
            .toJsonElement()
    return Json.encodeToString(obj)
  }
}

class AtomicSample {

  private val lock = reentrantLock()

  private val _x = atomic(0)

  val x
    get() = _x.value

  fun doWork(finalValue: Int) {
    check(x == 0)
    check(_x.getAndSet(3) == 0)
    check(x == 3)
    check(_x.compareAndSet(3, finalValue))
  }

  fun synchronizedFoo(value: Int) = lock.withLock { value }
}

enum class YesNo {
  YES,
  NO
}
