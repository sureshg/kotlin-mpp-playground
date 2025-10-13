package dev.suresh.db

import io.exoquery.*
import io.exoquery.annotation.*
import kotlinx.serialization.Serializable

@JvmInline @Serializable value class Email(val value: String)

@Serializable
data class People(
    val id: Long,
    val name: String,
    val email: Email?,
    val age: Int,
    val addressId: Long?,
)

@Serializable
data class Address(
    val id: Long,
    val street: String,
    val city: String,
    val state: String?,
    val zipCode: String?,
    val country: String,
)

@Serializable
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
  people.filter { it.name.like("aaa%").use } union people.filter { it.name.like("bbb%").use }
}

data class CommonType(val id: Long, val name: String)

// Map to common type and union
val commonType = sql {
  people.map { CommonType(it.id, it.name) } union robot.map { CommonType(it.id, it.name) }
}

fun select() {
  val s =
      sql.select {
        val p = from(people)
        val a = join(address) { it.id == p.addressId && it.city.like("%San Francisco%").use }
        where { p.age > 10 }
        groupBy(p.name, p.age)
        having { p95(p.age).use > 50 }
        sortBy(p.name to Ord.Asc, p.age to Ord.Desc)
        p to a
      }
  println("SQL: ${s.buildPrettyFor.Postgres().value}")
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
      setParams(p).onConflictUpdate(id) { excluding -> set(name to name + "-" + excluding.name) }
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

@CapturedFunction
// context(_: CapturedBlock)
fun String.like(value: String) =
    capture.expression { free("${this@like} LIKE $value").asPure<Boolean>() }

@CapturedFunction fun p95(measure: Int) = sql.expression { avg(measure) + 1.645 * stddev(measure) }

// data class User(val id: Long, val name: String, val active: Int)
//
// data class Comment(
//    val id: Long,
//    val content: String,
//    val userId: Long,
//    val createdAt: LocalDateTime
// )
//
// val user = sql { Table<User>() }
// val comment = sql { Table<Comment>() }
//
// fun select1() {
//  val date = Clock.System.now().plus(30.days).toLocalDateTime(TimeZone.currentSystemDefault())
//  val s = sql {
//    select {
//          val u = from(user)
//          val c = join(comment) { it.userId == u.id }
//          where { u.active == 1 && c.createdAt > param(date) }
//          groupBy(u.id)
//          u to count(c.id)
//        }
//        .filter { it.second > 5 }
//  }
//  println("SQL: ${s.buildPrettyFor.Postgres().value}")
// }
