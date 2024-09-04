package dev.suresh.lang

import dev.zacsweers.redacted.annotations.Redacted

data class Person(val name: Name, val address: Address, val privateInfo: PrivateInfo)

data class Name(val first: String, val last: String)

data class Address(val street: String, val city: String, val state: String, val zip: String)

data class PrivateInfo(@Redacted val ssn: String, @Redacted val dob: String)
