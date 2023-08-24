package dev.suresh

import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher

actual val platform: String = "JVM"

/** A coroutine dispatcher that executes tasks on Virtual Threads. */
val Dispatchers.VT by lazy { Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher() }
