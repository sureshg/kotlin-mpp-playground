package dev.suresh

import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.invoke.MethodHandle
import kotlin.jvm.optionals.getOrNull

val LINKER: Linker = Linker.nativeLinker()

val SYMBOL_LOOKUP: SymbolLookup by lazy {
  val stdlib = LINKER.defaultLookup()
  val loaderLookup = SymbolLookup.loaderLookup()
  SymbolLookup { name -> loaderLookup.find(name).or { stdlib.find(name) } }
}

val UNSAFE by lazy {
  sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe").run {
    isAccessible = true
    get(null) as sun.misc.Unsafe
  }
}

fun SymbolLookup.findOrNull(name: String): MemorySegment? = find(name).getOrNull()

fun downcallHandle(
    symbol: String,
    fdesc: FunctionDescriptor,
    vararg options: Linker.Option
): MethodHandle? =
    SYMBOL_LOOKUP.findOrNull(symbol).let { LINKER.downcallHandle(it, fdesc, *options) }
