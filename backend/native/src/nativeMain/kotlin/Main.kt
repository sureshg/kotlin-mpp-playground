import com.charleskorn.kaml.Yaml
import dev.suresh.Greeting
import dev.suresh.flow.timerComposeFlow
import dev.suresh.http.MediaApiClient
import io.matthewnelson.kmp.process.*
import kotlin.reflect.typeOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import wasm.execWasm

expect fun readPassword(prompt: String): String?

fun main(args: Array<String>): Unit = runBlocking {
  println("Kotlin Native App: ${BuildConfig.version}")
  println(Greeting().greeting())

  val count = args.firstOrNull()?.toIntOrNull() ?: 2
  execWasm(Path("./factorial.wasm"), arg = count)
  timerComposeFlow().take(count).collect(::println)

  println("Executing command...")
  val ps =
      Process.Builder(command = "ls")
          .args("-l")
          .destroySignal(Signal.SIGKILL)
          .stdout(Stdio.Inherit)
          .stderr(Stdio.Inherit)

  val exit = ps.spawn { it.waitForAsync(2.seconds) ?: -1 }
  println("Process exited: $exit")

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
  try {
    val images = client.images()
    println("Found ${images.size} images")
  } catch (e: Exception) {
    e.printStackTrace()
  }

  @Serializable data class Team(val leader: String, val members: List<String>)

  val yaml =
      Yaml.default.decodeFromString<Team>(
          """
          leader: Amy
          members:
                 - Bob
                 - Cindy
                 - Dan
          """
              .trimIndent())
  println(yaml)
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
