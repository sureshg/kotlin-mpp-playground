import arrow.continuations.SuspendApp
import dev.suresh.flow.timerComposeFlow
import dev.suresh.json
import dev.suresh.platform
import kotlinx.coroutines.flow.take
import kotlinx.serialization.encodeToString

fun main(args: Array<String>) = SuspendApp {
  // runBlocking is available only on jvm and native. Not available in common
  println("Kotlin Native App ${BuildConfig.version}")
  println(json.encodeToString(platform.info))
  val count = args.firstOrNull()?.toIntOrNull() ?: 5
  timerComposeFlow().take(count).collect(::println)
}
