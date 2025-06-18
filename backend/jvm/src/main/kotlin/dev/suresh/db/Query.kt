package dev.suresh.db

import io.exoquery.*

@JvmInline value class Email(val value: String)

data class People(
    val id: Long,
    val name: String,
    val email: Email?,
    val age: Int,
    val addressId: Long?
)

data class Address(
    val id: Long,
    val street: String,
    val city: String,
    val state: String?,
    val zipCode: String?,
    val country: String
)

data class Robot(
    val id: Long,
    val name: String,
)

typealias sql = capture

// Applicative capture
val people = sql { Table<People>() }
val address = sql { Table<Address>() }
val robot = sql { Table<Robot>() }

// Both people (Applicative capture) and pQuery (Direct capture) are isomorphic.
val pQuery =
    sql.select {
      val p = from(people)
      p
    }

val map = sql { people.map { it.name to it.age } }

val filter = sql { people.filter { it.age > 10 } }

// Correlated subqueries
val subQueries = sql { people.filter { it.age > people.map { it.age }.avg() } }

// Position aggregators
val aggregators = sql { people.map { count(it.name) to avg(it.age) } }

val distinct = sql { people.map { it.name to it.age }.distinct() }

val limitAndOffest = sql { people.drop(1).take(10) }

val union = sql {
  people.filter { it.name.startsWith("aaa") } union people.filter { it.name.startsWith("bbb") }
}

data class CommonType(val id: Long, val name: String)

// Map to common type and union
val commonTyep = sql {
  people.map { CommonType(it.id, it.name) } union robot.map { CommonType(it.id, it.name) }
}

fun select() {
  val s =
      sql.select {
        val p = from(people)
        val a = join(address) { a -> a.id == p.addressId }
        where { p.age > 10 }
        groupBy(p.name, p.age)
        sortBy(p.name to Ord.Asc, p.age to Ord.Desc)
        p to a
      }
  println(s.buildFor.Postgres().value)
}

fun insert(p: People) {
  val set = sql { insert<People> { set(name to "xxxx") } }

  sql {
    insert<People> { setParams(p).excluding(id) }
        .returning {
          it.id
          // Output(it.id, it.name)
        }
  }
}

fun upsert(p: People) {
  sql {
    insert<People> {
      setParams(p).onConflictUpdate(id) { excluding -> set(name to "name" + "-" + excluding.name) }
      // setParams(p).onConflictIgnore(id) - Do nothing
    }
  }
}

fun update(p: People) {
  sql { update<People> { setParams(p) }.where { id == param(p.id) } }
}

fun delete(p: People) {
  sql { delete<People>().where { id == param(p.id) } }
}

fun batch(p: Sequence<People>) {
  sql.batch(p) { p -> insert<People> { setParams(p).excluding(id) } }
}
