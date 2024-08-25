package dev.suresh

import java.util.concurrent.locks.ReentrantLock
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.LazyThreadSafetyMode.SYNCHRONIZED
import kotlin.concurrent.withLock

/**
 * Java Virtual thread friendly Kotlin lazy initialization. The implementation is based on
 * [Javalin Lazy](https://github.com/javalin/javalin/pull/1974) implementation.
 */
internal class ReentrantLazy<T : Any?>(initializer: () -> T) : Lazy<T> {
  private companion object {
    private object UNINITIALIZED_VALUE
  }

  private var initializer: (() -> T)? = initializer

  @Volatile private var lock: ReentrantLock? = ReentrantLock()

  @Volatile private var _value: Any? = UNINITIALIZED_VALUE

  override val value: T
    get() {
      lock?.withLock {
        if (_value === UNINITIALIZED_VALUE) {
          this._value = initializer!!.invoke()
          this.lock = null
          this.initializer = null
        }
      }
      @Suppress("UNCHECKED_CAST")
      return _value as T
    }

  override fun isInitialized() = _value !== UNINITIALIZED_VALUE
}

/** A virtual thread friendly [kotlin.lazy] implementation. */
fun <T : Any?> vtLazy(
    threadSafetyMode: LazyThreadSafetyMode = NONE,
    initializer: () -> T
): Lazy<T> =
    when (threadSafetyMode) {
      SYNCHRONIZED -> ReentrantLazy(initializer)
      else -> lazy(threadSafetyMode, initializer)
    }
