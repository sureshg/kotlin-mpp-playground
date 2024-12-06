package wasm

import io.github.charlietap.chasm.embedding.instance
import io.github.charlietap.chasm.embedding.invoke
import io.github.charlietap.chasm.embedding.module
import io.github.charlietap.chasm.embedding.shapes.Value
import io.github.charlietap.chasm.embedding.shapes.getOrNull
import io.github.charlietap.chasm.embedding.shapes.map
import io.github.charlietap.chasm.embedding.store
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

fun execWasm(path: Path, arg: Int = 5) {
  try {
    val exists = SystemFileSystem.metadataOrNull(path)?.isRegularFile == true
    if (exists) {
      println("Executing wasm: $path")
      val module = module(bytes = SystemFileSystem.source(path).buffered().readByteArray())
      val store = store()
      val instance = instance(store, module.getOrNull()!!, emptyList()).getOrNull()!!

      val result =
          invoke(store, instance, "iterFact", listOf(Value.Number.I32(arg))).map {
            (it.first() as Value.Number.I32).value
          }
      println("Result: $result")
    } else {
      println("Wasm file not found: $path")
    }
  } catch (e: Exception) {
    println("Failed to execute wasm: ${e.message}")
    e.printStackTrace()
  }
}