package dev.suresh

import dev.suresh.http.json
import dev.suresh.lang.*
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.io.bytestring.*
import kotlinx.serialization.encodeToString

class Greeting {

  fun greeting() = buildString {
    appendLine(json.encodeToString(platform.info))
    appendLine(data())
    appendLine(kotlinxTests())
    appendLine(atomicFUTests())
  }

  private fun kotlinxTests(): String {
    val ba = "Kotlinx".encodeToByteArray()
    val bs1 = ByteString(data = ba)
    val bs2 = "IO".encodeToByteString()

    val bs = buildByteString {
      append(bs1)
      append(" ".encodeToByteArray())
      append(bs2)
    }
    return bs.decodeToString()
  }

  private fun data() = buildString {
    val person =
        Person(
            name = Name(first = "Foo", last = "Bar"),
            address =
                Address(
                    street = "123 Main St", city = "San Francisco", state = "CA", zip = "95000"),
            privateInfo = PrivateInfo(ssn = "123-45-6789", dob = "01/01/2000"))
    val modPerson = person.copy()
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
