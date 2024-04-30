import dev.suresh.flow.timerComposeFlow
import dev.suresh.json
import dev.suresh.platform
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString

fun main(args: Array<String>): Unit = runBlocking {
  println("Kotlin Native App ${BuildConfig.version}")
  println(json.encodeToString(platform.info))
  val count = args.firstOrNull()?.toIntOrNull() ?: 5
  timerComposeFlow().take(count).collect(::println)
  println("Executing command...")
  execute("ls", "-l").also(::println)
}

data class ProcessResult(val code: Int, val rawOutput: String?)

expect fun execute(command: String, vararg args: String): ProcessResult
