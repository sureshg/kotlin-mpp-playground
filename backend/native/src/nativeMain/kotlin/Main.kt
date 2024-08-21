import dev.suresh.flow.timerComposeFlow
import dev.suresh.http.MediaApiClient
import dev.suresh.http.json
import dev.suresh.platform
import kotlin.reflect.typeOf
import kotlin.time.Duration
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString

data class ProcessResult(val code: Int, val rawOutput: String?)

expect fun execute(command: String, vararg args: String): ProcessResult

expect fun readPassword(prompt: String): String?

fun main(args: Array<String>): Unit = runBlocking {
  println("Kotlin Native App: ${BuildConfig.version}")
  println(json.encodeToString(platform.info))

  val count = args.firstOrNull()?.toIntOrNull() ?: 5
  timerComposeFlow().take(count).collect(::println)

  println("Executing command...")
  runCatching { execute("ls", "-l").also(::println) }
      .onFailure { println("Failed to execute command: ${it.message}") }

  println("Reflection Simple name ${this::class.simpleName}")
  println("Reflection Simple name ${this::class.qualifiedName}")
  println(prop("Hello"))
  val list = listOf("Hello", "Kotlin")
  println(prop(list))
  val nil: String? = null
  println(prop(nil))

  print("Enter your name: ")
  val user = readlnOrNull() ?: "Unknown"
  val password = readPassword("Enter your password: ")
  println(">>> User: $user, Password: $password")
  buffer()
  dir()
  // MultiplatformSystem.readEnvironmentVariable()

  val client = MediaApiClient()
  val images = client.images()
  println("Found ${images.size} images")
}

inline fun <reified T> prop(prop: T): String {
  val ktype = typeOf<T>()
  println("Type: ${ktype.classifier}")
  val result =
      when (ktype) {
        typeOf<String>() -> "String property"
        typeOf<List<String>>() -> "List<String> property"
        typeOf<Boolean>() -> "Boolean property"
        typeOf<Int>() -> "Int property"
        typeOf<Long>() -> "Long property"
        typeOf<Double>() -> "Double property"
        typeOf<Duration>() -> "Duration property"
        typeOf<String>() -> "String property"
        else -> "Unsupported type: ${typeOf<T>()}"
      }
  return if (ktype.isMarkedNullable) "Nullable $result" else result
}
