package dev.suresh.security

/** Password generator. This is not thread safe! */
class PasswordGen(
    private val lowercase: Boolean = true,
    private val uppercase: Boolean = true,
    private val number: Boolean = true,
    private val special: Boolean = true,
) {

  private var allowedChars: List<Char>

  init {
    check(lowercase || uppercase || number || special) { "At least one char type must be enabled" }
    allowedChars = buildList {
      if (lowercase) addAll('a'..'z')
      if (uppercase) addAll('A'..'Z')
      if (number) addAll('0'..'9')
      if (special) addAll("~!@#$%^&*+=".toList())
    }
  }

  fun generate(len: Int = 15): String {
    check(len > 0) { "Password length must be greater than 0" }
    allowedChars = allowedChars.shuffled()
    return buildString { repeat(len) { append(allowedChars.random()) } }
  }
}
