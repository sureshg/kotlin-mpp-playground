package dev.suresh

import com.sun.management.OperatingSystemMXBean
import java.io.File
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.StandardSocketOptions
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.Security
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

actual val platform: Platform = JvmPlatform

object JvmPlatform : Platform {

  private val log = LoggerFactory.getLogger(this::class.java)

  override val name: String = "JVM"

  override fun env(key: String, def: String?): String? = System.getenv(key) ?: def

  override fun sysProp(key: String, def: String?): String? = System.getProperty(key, def)

  override val tzShortId: String
    get() {
      // ZoneId.systemDefault().getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH)
      return DateTimeFormatter.ofPattern("zzz")
          .withLocale(Locale.ENGLISH)
          .withZone(ZoneId.systemDefault())
          .format(Instant.now())
    }

  /** A coroutine dispatcher that executes tasks on Virtual Threads. */
  override val virtualDispatcher: CoroutineDispatcher? by lazy {
    runCatching {
          log.info("Creating CoroutineDispatcher based on Java VirtualThreadPerTaskExecutor...")
          // Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()
          val mh =
              MethodHandles.publicLookup()
                  .findStatic(
                      Executors::class.java,
                      "newVirtualThreadPerTaskExecutor",
                      MethodType.methodType(
                          ExecutorService::class.java,
                      ))
          val execService = mh.invokeExact() as ExecutorService
          execService.asCoroutineDispatcher()
          // Via Reflection
          // (Executors::class.java.getMethod("newVirtualThreadPerTaskExecutor").invoke(null) as
          // ExecutorService).asCoroutineDispatcher()
        }
        .getOrNull()
  }

  override val appInfo: Map<String, String>
    get() =
        super.appInfo +
            mapOf(
                "uptime" to
                    run {
                      val processEpochSec =
                          ProcessHandle.current().info().startInstant().get().epochSecond
                      epochSecToString(processEpochSec)
                    })

  override val osInfo: Map<String, String?>
    get() =
        super.osInfo +
            mapOf(
                "name" to sysProp("os.name"),
                "version" to sysProp("os.version"),
                "arch" to sysProp("os.arch"),
                "user" to sysProp("user.name"),
            )
}

val Dispatchers.Virtual
  get() = platform.virtualDispatcher

val Dispatchers.VirtualOrIO
  get() = Virtual ?: IO

/**
 * Runs the given suspend block on [Dispatchers.Virtual], so that we can call blocking I/O APIs from
 * coroutines
 */
suspend inline fun <T> runOnVirtualThread(crossinline block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Virtual!!) { block() }

/** Creates a new coroutine scope that uses [Dispatchers.Virtual] as its dispatcher. */
val virtualThreadScope
  get() = CoroutineScope(Dispatchers.Virtual!!)

/** Returns the runtime information of the JVM and OS. */
fun jvmRuntimeInfo(debug: Boolean = false) = buildString {
  val rt = Runtime.getRuntime()
  val unit = 1024 * 1024L
  val heapSize = rt.totalMemory()
  val heapFreeSize = rt.freeMemory()
  val heapUsedSize = heapSize - heapFreeSize
  val heapMaxSize = rt.maxMemory()
  val osMxBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
  val rtMxBean = ManagementFactory.getRuntimeMXBean()

  appendLine("✧✧✧ Time: ${LocalDateTime.now()}")
  appendLine("✧✧✧ [JVM] JVM Version              : ${System.getProperty("java.version")}")

  appendLine("✧✧✧ [SYS-OS]  Operating System     : ${System.getProperty("os.name")}")
  appendLine("✧✧✧ [SYS-CPU] CPU Arch             : ${System.getProperty("os.arch")}")
  appendLine("✧✧✧ [SYS-CPU] Available Processors : ${rt.availableProcessors()}")
  appendLine("✧✧✧ [SYS-CPU] System CPU Usage     : ${osMxBean.cpuLoad}")
  appendLine("✧✧✧ [JVM-CPU] JVM CPU Usage        : ${osMxBean.processCpuLoad}")
  appendLine(
      "✧✧✧ [JVM-CPU] JVM CPU Time(Sec)    : ${Duration.ofNanos(osMxBean.processCpuTime).toSeconds()}")
  appendLine("✧✧✧ [SYS-MEM] Total Memory                  : ${osMxBean.totalMemorySize / unit} MiB")
  appendLine("✧✧✧ [SYS-MEM] Free  Memory                  : ${osMxBean.freeMemorySize / unit} MiB")
  appendLine("✧✧✧ [JVM-MEM] Current Heap Size (Committed) : ${heapSize / unit} MiB")
  appendLine("✧✧✧ [JVM-MEM] Current Free memory in Heap   : ${heapFreeSize/unit} MiB")
  appendLine("✧✧✧ [JVM-MEM] Currently used memory         : ${heapUsedSize/unit} MiB")
  appendLine("✧✧✧ [JVM-MEM] Max Heap Size (-Xmx)          : ${heapMaxSize/unit} MiB")

  appendLine("✧✧✧ JVM Input Arguments ✧✧✧")
  appendLine(rtMxBean.inputArguments)
  appendLine("✧✧✧ JVM Main class & Args ✧✧✧")
  appendLine(System.getProperty("sun.java.command"))

  appendLine("✧✧✧ Processes ✧✧✧")
  val ps = ProcessHandle.allProcesses().sorted(ProcessHandle::compareTo).toList()
  if (debug) {
    ps.forEach { appendLine("${it.pid()} : ${it.info()}") }
  } else {
    appendLine("Found ${ps.size} processes.")
  }

  appendLine("✧✧✧ Trust stores ✧✧✧")
  val caCerts =
      TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).run {
        init(null as KeyStore?)
        trustManagers.filterIsInstance<X509TrustManager>().flatMap { it.acceptedIssuers.toList() }
      }
  caCerts.forEach { appendLine(it.issuerX500Principal) }

  appendLine("✧✧✧ Dns Resolution ✧✧✧")
  val dns = InetAddress.getAllByName("google.com").toList()
  dns.forEach { appendLine(it) }

  appendLine("✧✧✧ TimeZones ✧✧✧")
  val tz = ZoneId.getAvailableZoneIds()
  if (debug) {
    tz.forEach { appendLine(it) }
  } else {
    appendLine("Found ${tz.size} timezones.")
  }

  appendLine("✧✧✧ Charsets ✧✧✧")
  val cs = Charset.availableCharsets()
  if (debug) {
    cs.forEach { (name, charset) -> appendLine("$name: $charset") }
  } else {
    appendLine("Found ${cs.size} charsets.")
  }

  appendLine("✧✧✧ System Locales ✧✧✧")
  val locales = Locale.getAvailableLocales()
  if (debug) {
    locales.forEach { appendLine(it) }
  } else {
    appendLine("Found ${locales.size} locales.")
  }

  appendLine("✧✧✧ System Countries ✧✧✧")
  val countries = Locale.getISOCountries()
  if (debug) {
    countries.forEach { appendLine(it) }
  } else {
    appendLine("Found ${countries.size} countries.")
  }

  appendLine("✧✧✧ System Currencies ✧✧✧")
  val currencies = Currency.getAvailableCurrencies()
  if (debug) {
    currencies.forEach { appendLine(it) }
  } else {
    appendLine("Found ${currencies.size} currencies.")
  }

  appendLine("✧✧✧ System Languages ✧✧✧")
  val languages = Locale.getISOLanguages()
  if (debug) {
    languages.forEach { appendLine(it) }
  } else {
    appendLine("Found ${languages.size} languages.")
  }

  appendLine("✧✧✧ Env Variables ✧✧✧")
  val env = System.getenv()
  env.forEach { (k: String, v: String) -> appendLine("$k : $v") }

  appendLine("✧✧✧ System Properties ✧✧✧")
  val props = System.getProperties()
  props.forEach { k: Any, v: Any -> appendLine("$k : $v") }

  val fmt = HexFormat.ofDelimiter(", ").withUpperCase().withPrefix("0x")
  appendLine("✧✧✧ I ❤️ Kotlin = ${fmt.formatHex("I ❤️ Kotlin".encodeToByteArray())}")
  appendLine("✧✧✧ LineSeparator  = ${fmt.formatHex(System.lineSeparator().encodeToByteArray())}")
  appendLine("✧✧✧ File PathSeparator = ${fmt.formatHex(File.pathSeparator.encodeToByteArray())}")
  appendLine("✧✧✧ File Separator = ${fmt.formatHex(File.separator.encodeToByteArray())}")

  appendLine("✧✧✧ Additional info in exception ✧✧✧")
  val ex =
      runCatching {
            Security.setProperty("jdk.includeInExceptions", "hostInfo,jar")
            Socket().use { s ->
              s.setOption(StandardSocketOptions.SO_REUSEADDR, true)
              s.setOption(StandardSocketOptions.SO_REUSEPORT, true)
              s.setOption(StandardSocketOptions.SO_KEEPALIVE, true)
              // Disable the Nagle algorithm as using it would hurt latency.
              s.setOption(StandardSocketOptions.TCP_NODELAY, true)
              // s.setOption(StandardSocketOptions.SO_RCVBUF, 4096)
              s.soTimeout = 100
              s.connect(InetSocketAddress("localhost", 12345), 10)
            }
          }
          .exceptionOrNull()
  appendLine(ex?.message)
  // check(ex?.message?.contains("localhost/127.0.0.1:12345") == true)

  appendLine(
      """
      +---------Summary-------+
      | Processes      : ${ps.size.toString().padEnd(5)}|
      | Dns Addresses  : ${dns.size.toString().padEnd(5)}|
      | Trust Stores   : ${caCerts.size.toString().padEnd(5)}|
      | TimeZones      : ${tz.size.toString().padEnd(5)}|
      | CharSets       : ${cs.size.toString().padEnd(5)}|
      | Locales        : ${locales.size.toString().padEnd(5)}|
      | Countries      : ${countries.size.toString().padEnd(5)}|
      | Languages      : ${languages.size.toString().padEnd(5)}|
      | Currencies     : ${currencies.size.toString().padEnd(5)}|
      | Env Vars       : ${env.size.toString().padEnd(5)}|
      | Sys Props      : ${props.size.toString().padEnd(5)}|
      | Virtual Thread : ${Thread.currentThread().isVirtual}|
      +-----------------------+
     """
          .trimIndent(),
  )
}
