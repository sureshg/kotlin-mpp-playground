package dev.suresh.lang

const val HAS_SEMI = 0x3B_3B_3B_3B_3B_3B_3B_3BL

/**
 * Has a zero byte in a long, copied straight from
 * [Hackers Delight](https://doc.lagout.org/security/Hackers%20Delight.pdf)
 *
 * Unlike in Java, hexadecimal integer literals in Kotlin can't represent negative values. So we
 * have to use [ULong] for literal values greater than [Long.MAX_VALUE]
 *
 * Eg:
 * ```kotlin
 *     val long = 0x80_80_80_80_80_80_80_80u.toLong()
 *     println(long.toString(16))
 *     println(long.toString(2))
 *     println(long.toULong().toString(16))
 *     println(long.toULong().toString(2))
 * ```
 *
 * Also see - [Stanford BitHacks](https://graphics.stanford.edu/~seander/bithacks.html)
 */
inline val Long.hasZeroByte
  get() = (this - 0x0101010101010101L) and this.inv() and 0x80_80_80_80_80_80_80_80u.toLong() != 0L

/** Check if a semi-column(0x3B) present in any of the eight bytes. */
inline val Long.hasSemi
  get() = (this xor HAS_SEMI).hasZeroByte
