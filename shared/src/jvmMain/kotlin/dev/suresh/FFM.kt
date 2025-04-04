package dev.suresh

import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.SymbolLookup
import java.lang.invoke.MethodHandle

val LINKER: Linker = Linker.nativeLinker()

/** Symbols loaded via caller's class loader (System.loadLibrary) if found, else from libc */
val SYMBOL_LOOKUP: SymbolLookup by lazy { SymbolLookup.loaderLookup().or(LINKER.defaultLookup()) }

val UNSAFE by lazy {
  sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe").run {
    isAccessible = true
    get(null) as sun.misc.Unsafe
  }
}

fun downcallHandle(
    symbol: String,
    fdesc: FunctionDescriptor,
    vararg options: Linker.Option
): MethodHandle? =
    SYMBOL_LOOKUP.findOrThrow(symbol).let { LINKER.downcallHandle(it, fdesc, *options) }
