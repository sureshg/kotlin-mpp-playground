package common

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.attributes.Attribute
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.*
import org.gradle.api.tasks.testing.logging.*
import org.gradle.jvm.toolchain.*
import org.gradle.kotlin.dsl.KotlinClosure2
import org.gradle.kotlin.dsl.assign
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.gradle.testing.internal.KotlinTestReport
import java.nio.file.Path

/** Java version properties. */
val Project.javaVersion
  get() = libs.versions.java.asProvider().map { JavaVersion.toVersion(it) }

val Project.javaRelease
  get() = javaVersion.map { it.majorVersion.toInt() }

val Project.toolchainVersion
  get() = javaVersion.map { JavaLanguageVersion.of(it.majorVersion) }

val Project.toolchainVendor
  get() = libs.versions.java.vendor.map(JvmVendorSpec::matching)

val Project.addModules
  get() = libs.versions.java.addModules.get()

val Project.githubUser
  get() = libs.versions.dev.name.get().lowercase()

val Project.githubRepo
  get() = "https://github.com/${githubUser}/${rootProject.name}"

/**
 * Retrieves all JVM arguments for running (**java**) and compiling (**javac**) java/kotlin code..
 *
 * @param compile Flag indicating whether to include additional JVM arguments for compilation.
 *                Defaults to false.
 * @return A list of JVM arguments for the project.
 */
fun Project.jvmArguments(compile: Boolean = false) = buildList {
  val jvmArgs = libs.versions.java.jvmArguments.get().split(",", " ").filter { it.isNotBlank() }
  addAll(jvmArgs)
  add("--add-modules=$addModules")
  if(compile.not()) {
    add("--show-version")
    add("--enable-native-access=ALL-UNNAMED")
  }
}

// $ java -XX:+UnlockExperimentalVMOptions -XX:+UnlockDiagnosticVMOptions -XX:+PrintFlagsFinal
// --version
// https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html
// https://docs.oracle.com/en/java/javase/21/core/java-networking.html#GUID-E6C82625-7C02-4AB3-B15D-0DF8A249CD73
// https://cs.oswego.edu/dl/jsr166/dist/jsr166.jar
// https://chriswhocodes.com/hotspot_options_openjdk21.html
// https://sap.github.io/SapMachine/jfrevents

val Project.cc get() =    listOf(
  "--show-version",
  "--enable-preview",
  "--add-modules=$addModules",
  "--enable-native-access=ALL-UNNAMED",
  "-XshowSettings:vm",
  "-XshowSettings:system",
  "-XshowSettings:properties",
  "-Xmx96M",
  "-XX:+PrintCommandLineFlags",
  "-XX:+UseZGC",
  "-XX:+ZGenerational",
  "-XX:+UseCompressedOops",
  "-XX:+UnlockExperimentalVMOptions",
  // os+thread,gc+heap=trace,
  """-Xlog:cds,safepoint,gc*:
              |file="$tmp$name-gc-%p-%t.log":
              |level,tags,time,uptime,pid,tid:
              |filecount=5,
              |filesize=10m"""
    .joinToConfigString(),
  """-XX:StartFlightRecording=
              |filename=$tmp$name.jfr,
              |name=$name,
              |maxsize=100M,
              |maxage=1d,
              |path-to-gc-roots=true,
              |dumponexit=true,
              |memory-leaks=gc-roots,
              |gc=detailed,
              |+jdk.VirtualThreadStart#enabled=true,
              |+jdk.VirtualThreadEnd#enabled=true,
              |jdk.ObjectCount#enabled=true,
              |jdk.SecurityPropertyModification#enabled=true,
              |jdk.TLSHandshake#enabled=true,
              |jdk.X509Certificate#enabled=true,
              |jdk.X509Validation#enabled=true,
              |settings=profile"""
    .joinToConfigString(),
  "-XX:FlightRecorderOptions:stackdepth=64",
  "-XX:+HeapDumpOnOutOfMemoryError",
  "-XX:HeapDumpPath=$tmp$name-%p.hprof",
  "-XX:ErrorFile=$tmp$name-hs-err-%p.log",
  "-XX:OnOutOfMemoryError='kill -9 %p'",
  "-XX:+ExitOnOutOfMemoryError",
  "-XX:+UnlockDiagnosticVMOptions",
  "-XX:NativeMemoryTracking=detail",
  "-XX:+EnableDynamicAgentLoading",
  "-XX:+LogVMOutput",
  "-XX:LogFile=$tmp$name-jvm.log",
  "-Djava.awt.headless=true",
  "-Djdk.attach.allowAttachSelf=true",
  "-Djdk.traceVirtualThreadLocals=false",
  "-Djdk.tracePinnedThreads=full",
  "-Djava.security.debug=properties",
  "-Djava.security.egd=file:/dev/./urandom",
  "-Djdk.includeInExceptions=hostInfo,jar",
  "-Dkotlinx.coroutines.debug",
  "-ea",
  // "--show-module-resolution",
  // "-XX:+ShowHiddenFrames",
  // "-XX:+AutoCreateSharedArchive",
  // "-XX:SharedArchiveFile=$tmp/$name.jsa"
  // "-verbose:module",
  // "-XX:ConcGCThreads=2",
  // "-XX:ZUncommitDelay=60",
  // "-XX:VMOptionsFile=vm_options",
  // "-Xlog:gc\*",
  // "-Xlog:class+load=info,cds=debug,cds+dynamic=info",
  // "-XX:+IgnoreUnrecognizedVMOptions",
  // "-XX:MaxRAMPercentage=0.8",
  // "-XX:+StartAttachListener", // For jcmd Dynamic Attach Mechanism
  // "-XX:+DisableAttachMechanism",
  // "-XX:+DebugNonSafepoints",
  // "-XX:OnOutOfMemoryError="./restart.sh"",
  // "-XX:SelfDestructTimer=0.05",
  // "-XX:NativeMemoryTracking=[off|summary|detail]",
  // "-XX:+PrintNMTStatistics",
  // "-XX:OnError=\"gdb - %p\"", // Attach gdb on segfault
  // "-Djava.security.properties=/path/to/custom/java.security", // == to override
  // "-Duser.timezone=\"PST8PDT\"",
  // "-Djava.net.preferIPv4Stack=true",
  // "-Djavax.net.debug=all",
  // "-Dhttps.protocols=TLSv1.2",
  // "-Dhttps.agent=$name",
  // "-Dhttp.keepAlive=true",
  // "-Dhttp.maxConnections=5",
  // "-Djava.security.manager=allow",
  // "-Dfile.encoding=COMPAT", // uses '-Dnative.encoding'
  // "-Djdbc.drivers=org.postgresql.Driver",
  // "-Djava.io.tmpdir=/var/data/tmp",
  // "-Djava.locale.providers=COMPAT,CLDR",
  // "-Djdk.lang.Process.launchMechanism=vfork",
  // "-Djdk.tls.maxCertificateChainLength=10",
  // "-Djdk.tls.maxHandshakeMessageSize=32768",
  // "--add-exports=java.management/sun.management=ALL-UNNAMED",
  // "--add-exports=jdk.attach/sun.tools.attach=ALL-UNNAMED",
  // "--add-opens=java.base/java.net=ALL-UNNAMED",
  // "--add-opens=jdk.attach/sun.tools.attach=ALL-UNNAMED",
  // "--patch-module java.base="$DIR/jsr166.jar",
  // "-javaagent:path/to/glowroot.jar",
  // "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",
  // "-agentlib:jdwp=transport=dt_socket,server=n,address=host:5005,suspend=y,onthrow=<FQ
  // exception class name>,onuncaught=<y/n>"
)

/** Kotlin version properties. */
val Project.kotlinVersion
  get() = libs.versions.kotlin.asProvider()

val Project.kotlinJvmTarget
  get() = libs.versions.kotlin.jvmtarget.map { JvmTarget.fromTarget(it) }

val Project.kotlinApiVersion
  get() = libs.versions.kotlin.api.version.map { KotlinVersion.fromVersion(it) }

val Project.kotlinLangVersion
  get() = libs.versions.kotlin.lang.version.map { KotlinVersion.fromVersion(it) }

/** Kotlin Dependencies extension functions. */
val Project.isKotlinMultiplatformProject
  get() = plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")

val Project.isKotlinJvmProject
  get() = plugins.hasPlugin("org.jetbrains.kotlin.jvm")

val Project.isKotlinJsProject
  get() = plugins.hasPlugin("org.jetbrains.kotlin.js")

/**
 * Returns the dependency string for the specified Kotlin wrapper.
 *
 * @param target The target wrapper to retrieve the dependency string for.
 * @return The dependency string for the specified Kotlin wrapper.
 */
fun kotlinw(target: String) = "org.jetbrains.kotlin-wrappers:kotlin-$target"

/**
 * Returns the dependency artifact for the given Gradle plugin.
 */
fun Provider<PluginDependency>.toDep() = map {
  "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}"
}

// https://kotlinlang.org/docs/multiplatform-set-up-targets.html#distinguish-several-targets-for-one-platform
val mppTargetAttr = Attribute.of("mpp.target.name", String::class.java)

var Project.mppTargetName: String
  get() = configurations.firstOrNull()?.attributes?.getAttribute(mppTargetAttr).orEmpty()
  set(value) {
    configurations.all {
      attributes.attribute(mppTargetAttr, value)
    }
  }

context(Project)
fun JavaToolchainSpec.configureJvmToolchain() {
  languageVersion = toolchainVersion
  vendor = toolchainVendor
}

context(Project)
fun JavaCompile.configureJavac() {
  modularity.inferModulePath = true
  options.apply {
    encoding = "UTF-8"
    release = javaRelease
    isIncremental = true
    isFork = true
    debugOptions.debugLevel = "source,lines,vars"
    // For Gradle worker daemon.
    forkOptions.jvmArgs?.addAll(jvmArguments(compile = true))
    // Javac options
    compilerArgs.addAll(
      jvmArguments(compile = true) + listOf(
        "-Xlint:all",
        "-parameters",
        // "-Xlint:-deprecation",       // suppress deprecations
        // "-Xlint:lossy-conversions",  // suppress lossy conversions
        // "-XX:+IgnoreUnrecognizedVMOptions",
        // "--add-exports",
        // "java.base/sun.nio.ch=ALL-UNNAMED",
        // "--patch-module",
        // "$moduleName=${sourceSets.main.get().output.asPath}",
        // "-Xplugin:unchecked",       // compiler plugin
      ),
    )
  }
}

context(Project)
fun KotlinCommonCompilerOptions.configureKotlinCommon() {
  apiVersion = kotlinApiVersion
  languageVersion = kotlinLangVersion
  progressiveMode = true
  allWarningsAsErrors = false
  suppressWarnings = false
  verbose = true
  freeCompilerArgs.addAll(
    "-Xcontext-receivers",
    "-Xallow-result-return-type",
    // "-P",
    // "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true",
    // "-P",
    // "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=...dir...",
  )
}

/**
 * JVM backend compiler options can be found in,
 *
 *  - [K2JVMCompilerArguments.kt](https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/K2JVMCompilerArguments.kt)
 *  - [JvmTarget.kt](https://github.com/JetBrains/kotlin/blob/master/compiler/config.jvm/src/org/jetbrains/kotlin/config/JvmTarget.kt)
 *  - [ApiVersion.kt](https://github.com/JetBrains/kotlin/blob/master/compiler/util/src/org/jetbrains/kotlin/config/ApiVersion.kt#L35)
 *
 *    Gradle Kotlin DSL Api/Lang versions,
 *  - [KotlinDslCompilerPlugins.kt](https://github.com/gradle/gradle/blob/master/subprojects/kotlin-dsl-plugins/src/main/kotlin/org/gradle/kotlin/dsl/plugins/dsl/KotlinDslCompilerPlugins.kt#L63-L64)
 */
context(Project)
fun KotlinJvmCompilerOptions.configureKotlinJvm() {
  jvmTarget = kotlinJvmTarget
  apiVersion = kotlinApiVersion
  languageVersion = kotlinLangVersion
  javaParameters = true
  verbose = true
  allWarningsAsErrors = false
  suppressWarnings = false
  freeCompilerArgs.addAll(
    "-Xadd-modules=$addModules",
    "-Xjsr305=strict",
    "-Xjvm-default=all",
    "-Xassertions=jvm",
    "-Xcontext-receivers",
    "-Xallow-result-return-type",
    "-Xemit-jvm-type-annotations",
    "-Xjspecify-annotations=strict",
    "-Xextended-compiler-checks",
    // "-Xjdk-release=$javaVersion",
    // "-Xadd-modules=ALL-MODULE-PATH",
    // "-Xmodule-path=",
    // "-Xjvm-enable-preview",
    // "-Xjavac-arguments=\"--add-exports java.base/sun.nio.ch=ALL-UNNAMED\"",
    // "-Xexplicit-api={strict|warning|disable}",
    // "-Xgenerate-strict-metadata-version",
    // "-Xuse-kapt4",
  )
}

context(Project)
fun LanguageSettingsBuilder.configureKotlinLang() {
  progressiveMode = true
  languageVersion = kotlinLangVersion.get().version
  optIn("kotlin.ExperimentalStdlibApi")
  optIn("kotlin.contracts.ExperimentalContracts")
  optIn("kotlin.ExperimentalUnsignedTypes")
  optIn("kotlin.io.path.ExperimentalPathApi")
  optIn("kotlin.io.encoding.ExperimentalEncodingApi")
  optIn("kotlin.time.ExperimentalTime")
  optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
  optIn("kotlinx.serialization.ExperimentalSerializationApi")
  optIn("kotlin.ExperimentalMultiplatform")
  optIn("kotlin.js.ExperimentalJsExport")
}

context(Project)
fun KotlinJvmTest.configureKotlinTest() {
  configureJavaTest()
}

context(Project)
fun Test.configureJavaTest() {
  useJUnitPlatform()
  jvmArgs(jvmArguments())
  reports.html.required = true
  maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

  testLogging {
    configureLogEvents()
  }

  afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
    if (desc.parent == null) { // will match the outermost suite
      println("""
             |
             |Test Results
             |------------
             |Tests     : ${result.resultType} (${result.testCount})
             |Successes : ${result.successfulTestCount}
             |Failures  : ${result.failedTestCount}
             |Skipped   : ${result.skippedTestCount}
             """.trimMargin()
      )
    }
  }))
}

fun TestLoggingContainer.configureLogEvents() {
  events = setOf(
    TestLogEvent.FAILED,
    TestLogEvent.PASSED,
    TestLogEvent.SKIPPED,
    TestLogEvent.STANDARD_OUT,
  )
  exceptionFormat = TestExceptionFormat.FULL
  showExceptions = true
  showCauses = true
  showStackTraces = true
  showStandardStreams = true

  // set options for log level DEBUG and INFO
  debug {
    events = setOf(
      TestLogEvent.STARTED,
      TestLogEvent.FAILED,
      TestLogEvent.PASSED,
      TestLogEvent.SKIPPED,
      TestLogEvent.STANDARD_ERROR,
      TestLogEvent.STANDARD_OUT
    )
    exceptionFormat = TestExceptionFormat.FULL
  }
}

context(Project)
fun KotlinTestReport.configureTestReport() {
}

context(Project)
fun KotlinJsOptions.configureKotlinJs() {
  // useEsClasses = true
  // sourceMap = true
  // sourceMapEmbedSources = "always"
  // freeCompilerArgs += listOf("-Xir-per-module")
}

context(Project)
fun KotlinNpmInstallTask.configureKotlinNpm() {
  //args.add("--ignore-engines")
}

/**
 * Adds a KSP dependency to the specified target in the project.
 *
 * @param targetName The name of the target to add the dependency to.
 * @param dependencyNotation The notation of the dependency to add.
 */
context(Project)
fun KotlinDependencyHandler.kspDependency(
  targetName: String, dependencyNotation: Any,
) {
  dependencies.add(
    "ksp${targetName.replaceFirstChar { it.uppercaseChar() }}",
    dependencyNotation,
  )
}


/** Returns the path of the dependency jar in runtime classpath. */
context(Project)
val ExternalDependency.dependencyPath get() = configurations
  .named("runtimeClasspath")
  .get()
  .resolvedConfiguration
  .resolvedArtifacts
  .find { it.moduleVersion.id.module == module }
  ?.file
  ?.path
  ?: error("Could not find $name in runtime classpath")


/** Returns the application `run` command. */
context(Project)
fun Path.appRunCmd(args: List<String>): String {
  val path = layout.projectDirectory.asFile.toPath().relativize(this)
  val newLine = System.lineSeparator()
  val lineCont = """\""" // Bash line continuation
  val indent = "\t"
  return args.joinToString(
    prefix = """
             |To Run the app,
             |${'$'} java -jar $lineCont $newLine
             """.trimMargin(),
    postfix = "$newLine$indent$path",
    separator = newLine,
  ) {
    // Escape the globstar
    "$indent$it $lineCont".replace("*", """\*""")
  }
}