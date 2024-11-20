package dev.suresh.wasm

import io.ktor.server.response.respondText
import io.ktor.server.routing.*

// val factModule by lazy { Parser.parse(::class.java.getResourceAsStream("/fact.wasm")!!) }

fun Routing.wasm() {
  get("/wasm") { call.respondText("WASM: WebAssembly") }
}

// https://github.com/extism/plugins/releases
