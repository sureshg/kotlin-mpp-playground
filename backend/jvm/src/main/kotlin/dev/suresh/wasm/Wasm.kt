package dev.suresh.wasm

import com.dylibso.chicory.compiler.MachineFactoryCompiler
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.types.FunctionType
import com.dylibso.chicory.wasm.types.ValType
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * More wasm modules can be found in
 * - [wasm-corpus](https://github.com/dylibso/chicory/tree/main/wasm-corpus/src/main/resources/compiled)
 * - [extism](https://github.com/extism/plugins/releases)
 */
val factWasmInst: Instance by lazy {
  val wasmRes = Thread.currentThread().contextClassLoader.getResourceAsStream("wasm/factorial.wasm")
  // val wasmRes = object {}.javaClass.getResourceAsStream("wasm/factorial.wasm")
  val wasmMod = Parser.parse(wasmRes)
  Instance.builder(wasmMod).withMachineFactory(MachineFactoryCompiler::compile).build()
}

fun Routing.wasm() {
  route("/wasm") {
    get("fact") {
      val num = call.parameters["num"]?.toLongOrNull() ?: 5

      val iterFact = factWasmInst.export("iterFact")
      val fact = iterFact.apply(num)[0]

      call.respondText("WASM: Factorial($num): $fact")
    }
  }
}

fun logFunction() =
    HostFunction(
        "console", "log", FunctionType.of(listOf(ValType.I32, ValType.I32), emptyList())) {
            instance,
            args ->
          val msg = instance.memory().readString(args[0].toInt(), args[1].toInt())
          println("WASM: $msg")
          // Value.i32(0)
          longArrayOf()
        }
