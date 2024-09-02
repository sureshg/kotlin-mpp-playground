package dev.suresh.lang

import com.javiersc.kotlin.kopy.Kopy
import dev.zacsweers.redacted.annotations.Redacted

@Kopy data class Person(val name: Name, val address: Address, val privateInfo: PrivateInfo)

@Kopy data class Name(val first: String, val last: String)

@Kopy data class Address(val street: String, val city: String, val state: String, val zip: String)

data class PrivateInfo(@Redacted val ssn: String, @Redacted val dob: String)
