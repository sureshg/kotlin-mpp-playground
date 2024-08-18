package dev.suresh.qos

import dev.suresh.downcallHandle
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.ValueLayout.JAVA_INT
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.asCoroutineDispatcher

/** Constrain JVM threads and coroutines to the efficiency cores available on M-series Macs */
object FFMQosSetter {
  fun setQosClass(qosClass: QosClass, relativePriority: Int = 0): Int {
    val result =
        downcallHandle(
                "pthread_set_qos_class_self_np",
                FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT))
            ?.invokeExact(qosClass.raw, relativePriority) as Int

    when (result != 0) {
      true -> System.err.println("Failed to set QoS class, error code: $result")
      else -> println("QoS class set successfully.")
    }
    return result
  }
}

/**
 * Refer to the
 * [Energy Efficiency Guide for Mac Apps](https://developer.apple.com/library/archive/documentation/Performance/Conceptual/power_efficiency_guidelines_osx/PrioritizeWorkAtTheTaskLevel.html#//apple_ref/doc/uid/TP40013929-CH35-SW5)
 */
enum class QosClass(val raw: Int) {
  QOS_CLASS_USER_INTERACTIVE(0x21),
  QOS_CLASS_USER_INITIATED(0x19),
  QOS_CLASS_DEFAULT(0x15),
  QOS_CLASS_UTILITY(0x11),
  QOS_CLASS_BACKGROUND(0x09),
  QOS_CLASS_UNSPECIFIED(0x00),
}

val BackgroundQosCoroutineDispatcher by lazy {
  ThreadPoolExecutor(
          /* corePoolSize = */ 0,
          /* maximumPoolSize = */ Runtime.getRuntime().availableProcessors(),
          /* keepAliveTime = */ 60L,
          /* unit = */ TimeUnit.SECONDS,
          /* workQueue = */ SynchronousQueue(),
          /* threadFactory = */ object : ThreadFactory {

            override fun newThread(r: Runnable): Thread {
              val fact = Executors.defaultThreadFactory()
              return fact.newThread(r).apply {
                FFMQosSetter.setQosClass(QosClass.QOS_CLASS_BACKGROUND)
                name = "QosThreadPool-${threadId()}"
              }
            }
          })
      .asCoroutineDispatcher()
}
