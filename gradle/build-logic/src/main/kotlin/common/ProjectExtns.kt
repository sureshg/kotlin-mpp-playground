package common

import com.google.devtools.ksp.gradle.KspAATask
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.attributes.*
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.*
import org.gradle.api.tasks.testing.logging.*
import org.gradle.internal.os.OperatingSystem
import org.gradle.jvm.toolchain.*
import org.gradle.kotlin.dsl.*
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.testing.internal.KotlinTestReport

// val logger = LoggerFactory.getLogger("build-logic")

/** Returns version catalog of this project. */
internal val Project.libs
  get() = the<LibrariesForLibs>()

/**
 * Returns version catalog extension of this project. Give access to all version catalogs available.
 */
internal val Project.catalogs
  get() = the<VersionCatalogsExtension>()

/** Quote for -Xlog file */
val Project.xQuote
  get() = if (OperatingSystem.current().isWindows) """\"""" else """""""

val Project.sharedProjectName
  get() = "shared"

// val debug: String? by project
val Project.debugEnabled
  get() = properties["debug"]?.toString().toBoolean()

val Project.skipTest
  get() = providers.gradleProperty("skip.test").map(String::toBoolean).getOrElse(false)

val Project.hasCleanTask
  get() = gradle.startParameter.taskNames.any { it == "clean" }

val Project.hasDokkaTasks
  get() = gradle.taskGraph.allTasks.filterIsInstance<AbstractDokkaTask>().any()

val Project.isSnapshot
  get() = version.toString().endsWith("SNAPSHOT", true)

val Project.runsOnCI
  get() = providers.environmentVariable("CI").getOrElse("false").toBoolean()

val Project.composeReportsEnabled
  get() = hasProperty("composeCompilerReports")

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

val Project.isRootProject
  get() = this == rootProject

val Project.githubUser
  get() = libs.versions.dev.name.get().lowercase()

val Project.githubRepo
  get() = "https://github.com/${githubUser}/${rootProject.name}"

/** For publishing to maven central and GitHub */
val Project.signingKey
  get() = providers.gradleProperty("signingKey")

val Project.signingKeyId
  get() = providers.gradleProperty("signingKeyId")

val Project.signingPassword
  get() = providers.gradleProperty("signingPassword")

val Project.hasSigningKey
  get() =
      signingKeyId.orNull.isNullOrBlank().not() &&
          signingKey.orNull.isNullOrBlank().not() &&
          signingPassword.orNull.isNullOrBlank().not()

val Project.mavenCentralUsername
  get() = providers.gradleProperty("mavenCentralUsername")

val Project.mavenCentralPassword
  get() = providers.gradleProperty("mavenCentralPassword")

val Project.githubActor
  get() = providers.gradleProperty("githubActor")

val Project.githubToken
  get() = providers.gradleProperty("githubToken")

/**
 * JVM arguments for running (**java**) or compiling (**javac**) java/kotlin build tasks.
 * - [Java-Command](https://docs.oracle.com/en/java/javase/20/docs/specs/man/java.html)
 * - [Java-Networking-Parameters](https://docs.oracle.com/en/java/javase/20/core/java-networking.html#GUID-E6C82625-7C02-4AB3-B15D-0DF8A249CD73)
 * - [JSR166](https://cs.oswego.edu/dl/jsr166/dist/jsr166.jar)
 * - [Hotspot Options](https://chriswhocodes.com/hotspot_option_differences.html)
 * - [JFR Events](https://sap.github.io/SapMachine/jfrevents)
 *
 * @param appRun Specifies if the JVM arguments are to be used for running the application. Default
 *   value is `false`.
 * @param headless Specifies if the application is to be run in headless mode. Default value is
 *   `true`.
 * @return A list of JVM arguments for the project.
 */
fun Project.jvmArguments(appRun: Boolean = false, headless: Boolean = true) = buildList {
  val jvmArgs = libs.versions.java.jvmArguments.get().split(",", " ").filter { it.isNotBlank() }
  addAll(jvmArgs)
  add("--add-modules=$addModules")
  // add("--add-opens=java.base/jdk.internal.classfile=ALL-UNNAMED")
  // add("--add-opens=java.base/jdk.internal.classfile.constantpool=ALL-UNNAMED")
  // add("--add-opens=java.base/jdk.internal.classfile.instruction=ALL-UNNAMED")
  // 'java' arguments.
  if (appRun) {
    addAll(
        listOf(
            "--show-version",
            "-XX:+PrintCommandLineFlags",
            "--enable-native-access=ALL-UNNAMED",
            "-Xms64M",
            "-Xmx96M",
            "-XX:+UseZGC",
            "-XX:+ZGenerational",
            "-XX:+UseCompressedOops",
            "-XX:+UseStringDeduplication",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:MaxRAMPercentage=0.8",
            "-XX:LockingMode=2",
            // "-XX:+UseEpsilonGC",
            // "-XX:+AlwaysPreTouch",
            // os+thread,gc+heap=trace,
            """-Xlog:gc*,stringdedup*:
              |file="$tmp$name-gc-%p-%t.log":
              |level,tags,time,uptime,pid,tid:
              |filecount=5,
              |filesize=10m"""
                .joinToConfigString(),
            """-Xlog:class+load:file=$tmp$name-cds.log:
                |uptime,level,tags,pid:filesize=0"""
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
            // "-XX:+ErrorFileToStderr",
            "-XX:+ExitOnOutOfMemoryError",
            "-XX:+UnlockDiagnosticVMOptions",
            "-XX:NativeMemoryTracking=detail",
            "-XX:+EnableDynamicAgentLoading",
            "-XX:+LogVMOutput",
            "-XX:LogFile=$tmp$name-jvm.log",
            "-Djdk.attach.allowAttachSelf=true",
            "-Djdk.traceVirtualThreadLocals=false",
            "-Djdk.tracePinnedThreads=full",
            "-Djava.security.debug=properties",
            "-Djava.security.egd=file:/dev/./urandom",
            "-Djdk.includeInExceptions=hostInfo,jar",
            "-Dkotlinx.coroutines.debug",
            "-Dcom.sun.management.jmxremote",
            "-Dcom.sun.management.jmxremote.local.only=false",
            "-Dcom.sun.management.jmxremote.port=9898",
            "-Dcom.sun.management.jmxremote.host=0.0.0.0",
            "-Dcom.sun.management.jmxremote.rmi.port=9898",
            "-Dcom.sun.management.jmxremote.authenticate=false",
            "-Dcom.sun.management.jmxremote.ssl=false",
            "-Djava.rmi.server.hostname=0.0.0.0",
            "-Dio.ktor.development=${project.hasProperty("development")}",
            // "-XX:OnOutOfMemoryError='kill -9 %p'",
            // "-ea",
            // "-XshowSettings:vm",
            // "-XshowSettings:system",
            // "-XshowSettings:properties",
            // "--show-module-resolution",
            // "-XX:+UseCompactObjectHeaders",
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
            // "-Dhttps.protocols=TLSv1.3",
            // "-Dhttps.agent=$name",
            // "-Dhttp.keepAlive=true",
            // "-Dhttp.maxConnections=5",
            // ----- Java HTTP Client -----
            // "-Djdk.internal.httpclient.disableHostnameVerification",
            // "-Djdk.httpclient.HttpClient.log=headers",
            // "-Djdk.internal.httpclient.debug=false",
            // "-Djdk.tls.client.protocols=\"TLSv1.2,TLSv1.3\"",
            // ----- Misc -----
            // "-Djava.security.manager=allow",
            // "-Dfile.encoding=COMPAT", // uses '-Dnative.encoding'
            // "-Djdbc.drivers=org.postgresql.Driver",
            // "-Djava.io.tmpdir=/var/data/tmp",
            // "-Djava.locale.providers=COMPAT,CLDR",
            // "-Djdk.lang.Process.launchMechanism=vfork",
            // "-Djdk.tls.maxCertificateChainLength=10",
            // "-Djdk.tls.maxHandshakeMessageSize=32768",
            // "-Djdk.virtualThreadScheduler.parallelism=10",
            // "-Djdk.virtualThreadScheduler.maxPoolSize=256",
            // "--add-exports=java.management/sun.management=ALL-UNNAMED",
            // "--add-exports=jdk.attach/sun.tools.attach=ALL-UNNAMED",
            // "--add-opens=java.base/java.net=ALL-UNNAMED",
            // "--add-opens=jdk.attach/sun.tools.attach=ALL-UNNAMED",
            // "--patch-module java.base="$DIR/jsr166.jar",
            // "-javaagent:path/to/glowroot.jar",
            // "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005",
            // "-agentlib:jdwp=transport=dt_socket,server=n,address=host:5005,suspend=y,onthrow=<FQ
            // exception class name>,onuncaught=<y/n>"
        ))

    if (headless) {
      add("-Djava.awt.headless=true")
    }
  }
}

/**
 * Returns the dependency string for the specified Kotlin wrapper.
 *
 * @param target The target wrapper to retrieve the dependency string for.
 * @return The dependency string for the specified Kotlin wrapper.
 */
fun kotlinw(target: String) = "org.jetbrains.kotlin-wrappers:kotlin-$target"

/** Returns the dependency artifact for the given Gradle plugin. */
fun Provider<PluginDependency>.toDep() = map {
  "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}"
}

// https://kotlinlang.org/docs/multiplatform-set-up-targets.html#distinguish-several-targets-for-one-platform
val mppTargetAttr = Attribute.of("mpp.target.name", String::class.java)

context(Project)
fun KotlinTarget.setTargetAttribute() {
  attributes.attribute(mppTargetAttr, targetName)
}

context(Project)
fun JavaToolchainSpec.configureJvmToolchain() {
  languageVersion = toolchainVersion
  vendor = toolchainVendor
}

context(Project)
fun JavaCompile.configureJavac() {

  options.apply {
    encoding = "UTF-8"
    release = javaRelease
    isIncremental = true
    isFork = true
    debugOptions.debugLevel = "source,lines,vars"
    // For Gradle worker daemon.
    forkOptions.jvmArgs?.addAll(jvmArguments())
    // Javac options
    compilerArgs.addAll(
        jvmArguments() +
            listOf(
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
      buildList {
        add("-Xcontext-receivers")
        add("-Xexpect-actual-classes")
        if (composeReportsEnabled) {
          val reportPath = layout.buildDirectory.dir("compose_compiler").get().asFile.absolutePath
          add("-P")
          add("plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$reportPath")
          add("-P")
          add("plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$reportPath")
        }
      })
}

context(Project)
fun KspAATask.configureKspConfig() {
  kspConfig.apply {
    apiVersion = kotlinApiVersion.map { it.version }
    jvmTarget = kotlinJvmTarget.map { it.target }
    languageVersion = kotlinLangVersion.map { it.version }
    allWarningsAsErrors = false
  }
}

/**
 * JVM backend compiler options can be found in,
 * - [K2JVMCompilerArguments.kt](https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/K2JVMCompilerArguments.kt)
 * - [JvmTarget.kt](https://github.com/JetBrains/kotlin/blob/master/compiler/config.jvm/src/org/jetbrains/kotlin/config/JvmTarget.kt)
 * - [ApiVersion.kt](https://github.com/JetBrains/kotlin/blob/master/compiler/util/src/org/jetbrains/kotlin/config/ApiVersion.kt#L35)
 *
 *   Gradle Kotlin DSL Api/Lang versions,
 * - [KotlinDslCompilerPlugins.kt](https://github.com/gradle/gradle/blob/master/platforms/core-configuration/kotlin-dsl-plugins/src/main/kotlin/org/gradle/kotlin/dsl/plugins/dsl/KotlinDslCompilerPlugins.kt#L63-L64)
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
  // enableLanguageFeature("ContextReceivers")
  optIn("kotlin.ExperimentalStdlibApi")
  optIn("kotlin.contracts.ExperimentalContracts")
  optIn("kotlin.ExperimentalUnsignedTypes")
  optIn("kotlin.io.encoding.ExperimentalEncodingApi")
  optIn("kotlin.time.ExperimentalTime")
  optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
  optIn("kotlinx.serialization.ExperimentalSerializationApi")
  optIn("kotlin.ExperimentalMultiplatform")
  optIn("kotlin.js.ExperimentalJsExport")
  optIn("kotlin.experimental.ExperimentalNativeApi")
  optIn("kotlinx.cinterop.ExperimentalForeignApi")
  // optIn("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
}

context(Project)
fun Test.configureJavaTest() {
  useJUnitPlatform()
  jvmArgs(jvmArguments())
  // For JUnit5 @EnabledIfSystemProperty
  systemProperty("ktorTest", project.hasProperty("ktorTest"))
  systemProperty("k8sTest", project.hasProperty("k8sTest"))
  reports.html.required = true
  maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

  testLogging { configureLogEvents() }
  // timeout = 10.minutes.toJavaDuration()

  afterSuite(
      KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
        if (desc.parent == null) { // will match the outermost suite
          println(
              """
              |
              |Test Results
              |------------
              |Tests     : ${result.resultType} (${result.testCount})
              |Successes : ${result.successfulTestCount}
              |Failures  : ${result.failedTestCount}
              |Skipped   : ${result.skippedTestCount}
              """
                  .trimMargin())
        }
      }))
}

fun TestLoggingContainer.configureLogEvents() {
  events =
      setOf(
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
    events =
        setOf(
            TestLogEvent.STARTED,
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_ERROR,
            TestLogEvent.STANDARD_OUT)
    exceptionFormat = TestExceptionFormat.FULL
  }
}

context(Project)
fun KotlinTestReport.configureTestReport() {}

context(Project)
fun KotlinJsCompilerOptions.configureKotlinJs() {
  useEsClasses = true
  // sourceMap = true
  // sourceMapEmbedSources = "always"
  // freeCompilerArgs += listOf("-Xir-per-module")
}

context(Project)
fun KotlinNpmInstallTask.configureKotlinNpm() {
  // args.add("--ignore-engines")
}

/**
 * Adds a KSP dependency to the specified target in the project.
 *
 * @param targetName The name of the target to add the dependency to.
 * @param dependencyNotation The notation of the dependency to add.
 */
context(Project)
fun KotlinDependencyHandler.kspDep(
    targetName: String,
    dependencyNotation: Any,
) {
  dependencies.add(
      "ksp${targetName.replaceFirstChar { it.uppercaseChar() }}",
      dependencyNotation,
  )
}

/** Returns the path of the dependency jar in runtime classpath. */
context(Project)
val ExternalDependency.dependencyPath
  get() =
      configurations
          .named("runtimeClasspath")
          .get()
          .resolvedConfiguration
          .resolvedArtifacts
          .find { it.moduleVersion.id.module == module }
          ?.file
          ?.path ?: error("Could not find $name in runtime classpath")

/** Returns the application `run` command. */
context(Project)
fun Path.appRunCmd(args: List<String>): String {
  val path = layout.projectDirectory.asFile.toPath().relativize(this)
  val newLine = System.lineSeparator()
  val lineCont = """\""" // Bash line continuation
  val indent = "\t"
  return args.joinToString(
      prefix =
          """
             |To Run the app,
             |${'$'} java -jar $lineCont $newLine
             """
              .trimMargin(),
      postfix = "$newLine$indent$path",
      separator = newLine,
  ) {
    // Escape the globstar
    "$indent$it $lineCont".replace("*", """\*""")
  }
}

/** Returns the JDK install path provided by the [JavaToolchainService] */
val Project.javaToolchainPath
  get(): Path {
    val defToolchain = extensions.findByType(JavaPluginExtension::class)?.toolchain
    val javaToolchainSvc = extensions.findByType(JavaToolchainService::class)
    // val jvm: JavaVersion? = org.gradle.internal.jvm.Jvm.current().javaVersion

    val jLauncher =
        when (defToolchain != null) {
          true -> javaToolchainSvc?.launcherFor(defToolchain)
          else -> javaToolchainSvc?.launcherFor { configureJvmToolchain() }
        }?.orNull

    return jLauncher?.metadata?.installationPath?.asFile?.toPath()
        ?: error("Requested JDK version ($javaVersion) is not available.")
  }

/** Return incubator modules of the tool chain JDK */
val Project.incubatorModules
  get(): String {
    val javaCmd = project.javaToolchainPath.resolve("bin").resolve("java")
    val bos = ByteArrayOutputStream()
    val execResult = exec {
      workingDir = layout.buildDirectory.get().asFile
      commandLine = listOf(javaCmd.toString())
      args = listOf("--list-modules")
      standardOutput = bos
      errorOutput = bos
    }
    execResult.assertNormalExitValue()
    return bos.toString(Charsets.UTF_8)
        .lines()
        .filter { it.startsWith("jdk.incubator") }
        .joinToString(",") { it.substringBefore("@").trim() }
  }

/**
 * Retrieves a map of version aliases and their corresponding versions from the project's version
 * catalog.
 *
 * @param name The name of the version catalog. Defaults to "libs" if not specified.
 * @return A map where the keys are the version aliases and the values are their corresponding
 *   versions.
 */
fun Project.versionCatalogMapOf(name: String = "libs") = run {
  val catalog = catalogs.named(name)
  catalog.versionAliases.associateWith { catalog.findVersion(it).get().toString() }
}

/**
 * Print all the catalog version strings and it's values.
 *
 * [VersionCatalogsExtension](https://docs.gradle.org/current/userguide/platforms.html#sub:type-unsafe-access-to-catalog)
 */
fun Project.printVersionCatalog() {
  if (debugEnabled) {
    catalogs.catalogNames.map { cat ->
      println("=== Catalog $cat ===")
      val catalog = catalogs.named(cat)
      catalog.versionAliases.forEach { alias ->
        println("${alias.padEnd(30, '-')}-> ${catalog.findVersion(alias).get()}")
      }
    }
  }
}

/** Print all the tasks */
fun Project.printTaskGraph() {
  if (debugEnabled) {
    gradle.taskGraph.whenReady {
      allTasks.forEachIndexed { index, task -> println("${index + 1}. ${task.name}") }
    }
  }
}

/** Adds [file] as an outgoing variant to publication. */
@Suppress("UnstableApiUsage")
fun Project.addFileToJavaComponent(file: File) {
  // Here's a configuration to declare the outgoing variant
  val executable: Configuration by
      configurations.consumable("executable") {
        description = "Declares executable outgoing variant"
        attributes {
          // See https://docs.gradle.org/current/userguide/variant_attributes.html
          attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
          attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("exe"))
        }
        outgoing { artifact(file) { classifier = "bin" } }
      }
  val javaComponent = components.findByName("java") as AdhocComponentWithVariants
  javaComponent.addVariantsFromConfiguration(executable) {
    // dependencies for this variant are considered runtime dependencies
    mapToMavenScope("runtime")
    // and also optional dependencies, because we don't want them to leak
    mapToOptional()
  }
}

/** Lazy version of [TaskContainer.maybeCreate] */
inline fun <reified T : Task> TaskContainer.maybeRegister(
    name: String,
    action: Action<in T>? = null,
) =
    when (name) {
      // val taskCollection = withType<T>().matching { it.name == name }
      in names -> named<T>(name = name)
      else -> register<T>(name = name)
    }.also {
      if (action != null) {
        it.configure(action)
      }
    }
