package dev.suresh.wasm

import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Routing.wasm() {
  get("/wasm") { call.respondText("WASM: WebAssembly") }
}

// https://github.com/extism/plugins/releases
