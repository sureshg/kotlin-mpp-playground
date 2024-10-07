package dev.suresh.system

object SystemExitInvoker {
  operator fun invoke() = System.exit(0)
}
