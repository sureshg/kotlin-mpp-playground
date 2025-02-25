package dev.suresh

import java.time.*
import java.util.*

fun main() {
  println("JVM Certs & Security!")
  val trustStores = TrustStore.allTrustStores()
  trustStores.forEach { println(it) }

  val jj = Byte::class.javaObjectType
  println(jj)
  val valueClasses =
      arrayOf(
          Byte::class.java,
          Short::class.java,
          Int::class.java,
          Long::class.java,
          Float::class.java,
          Double::class.java,
          Boolean::class.java,
          Char::class.java,
          Number::class.java,
          java.lang.Byte::class.java,
          java.lang.Short::class.java,
          java.lang.Integer::class.java,
          java.lang.Long::class.java,
          java.lang.Float::class.java,
          java.lang.Double::class.java,
          java.lang.Boolean::class.java,
          java.lang.Character::class.java,
          java.lang.Number::class.java,
          Byte::class.javaPrimitiveType,
          Short::class.javaPrimitiveType,
          Int::class.javaPrimitiveType,
          Long::class.javaPrimitiveType,
          Float::class.javaPrimitiveType,
          Double::class.javaPrimitiveType,
          Boolean::class.javaPrimitiveType,
          Char::class.javaPrimitiveType,
          Number::class.javaPrimitiveType,
          Byte::class.javaObjectType,
          Short::class.javaObjectType,
          Int::class.javaObjectType,
          Long::class.javaObjectType,
          Float::class.javaObjectType,
          Double::class.javaObjectType,
          Boolean::class.javaObjectType,
          Char::class.javaObjectType,
          Number::class.javaObjectType,
          Record::class.java,
          Duration::class.java,
          Instant::class.java,
          LocalDate::class.java,
          LocalDateTime::class.java,
          LocalTime::class.java,
          MonthDay::class.java,
          OffsetDateTime::class.java,
          OffsetTime::class.java,
          Optional::class.java,
          OptionalDouble::class.java,
          OptionalInt::class.java,
          OptionalLong::class.java,
          Period::class.java,
          Year::class.java,
          YearMonth::class.java,
          ZonedDateTime::class.java,
      )

  // This will all become primitive in Valhalla
  valueClasses.forEach {
    println("${it?.simpleName?.padEnd(15)} ->  ${it?.typeName?.padEnd(25)}  ${it?.isPrimitive}")
  }
}
