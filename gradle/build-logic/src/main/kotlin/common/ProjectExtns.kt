@file:Suppress("UnstableApiUsage")

package common

import com.google.devtools.ksp.gradle.KspAATask
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Path
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.jar.Attributes
import org.graalvm.buildtools.gradle.dsl.GraalVMExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.component.*
import org.gradle.api.attributes.*
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.*
import org.gradle.api.tasks.testing.logging.*
import org.gradle.internal.os.OperatingSystem
import org.gradle.jvm.toolchain.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// val logger = LoggerFactory.getLogger("build-logic")

/** Returns the version catalog of this project. */
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

val Project.isRootProject
  get() = this == rootProject

val Project.sharedProjectName
  get() = "shared"

val Project.buildLogicProjectName
  get() = "build-logic"

val Project.isSharedProject
  get() = name == sharedProjectName

val Project.skipTest
  get() = gradleBooleanProp("skip.test").get()

val Project.hasCleanTask
  get() = gradle.startParameter.taskNames.any { it in listOf("clean", "cleanAll") }

val Project.runsOnCI
  get() = providers.environmentVariable("CI").isPresent

// val debug: String? by project
val Project.debugEnabled
  get() = gradleBooleanProp("debug").get()

val Project.isSnapshotVersion
  get() = version.toString().endsWith("-SNAPSHOT", true)

val Project.isNativeTargetEnabled: Boolean
  get() = gradleBooleanProp("kotlin.target.native.enabled").get()

val Project.isWinTargetEnabled: Boolean
  get() = gradleBooleanProp("kotlin.target.win.enabled").get()

val Project.isComposeModuleEnabled: Boolean
  get() = gradleBooleanProp("module.compose.enabled").get()

val Project.isBootModuleEnabled: Boolean
  get() = gradleBooleanProp("module.boot.enabled").get()

val Project.isMvnSnapshotRepoEnabled: Boolean
  get() = gradleBooleanProp("maven.snapshot.repo.enabled").get()

val Project.isAutomaticModuleEnabled
  get() = gradleBooleanProp("java.automatic.module.enabled").get()

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
  get() = libs.versions.java.addmodules.get()

/** Kotlin version properties. */
val Project.kotlinVersion
  get() = libs.versions.kotlin.asProvider()

val Project.kotlinJvmTarget
  get() = libs.versions.kotlin.jvmtarget.map { JvmTarget.fromTarget(it) }

val Project.kotlinApiVersion
  get() = libs.versions.kotlin.api.version.map { KotlinVersion.fromVersion(it) }

val Project.kotlinLangVersion
  get() = libs.versions.kotlin.lang.version.map { KotlinVersion.fromVersion(it) }

val Project.orgName
  get() = libs.versions.org.name.get()

val Project.orgUrl
  get() = libs.versions.org.url.get()

val Project.githubUser
  get() = libs.versions.dev.name.get().lowercase()

val Project.githubRepo
  get() = "https://github.com/${githubUser}/${rootProject.name}"

/** For publishing to maven central and GitHub */
val Project.signingInMemoryKey
  get() = providers.gradleProperty("signingInMemoryKey")

val Project.signingInMemoryKeyId
  get() = providers.gradleProperty("signingInMemoryKeyId")

val Project.signingInMemoryKeyPassword
  get() = providers.gradleProperty("signingInMemoryKeyPassword")

val Project.hasSigningKey
  get() =
      signingInMemoryKey.orNull.isNullOrBlank().not() &&
          signingInMemoryKeyPassword.orNull.isNullOrBlank().not()

val Project.mavenCentralUsername
  get() = providers.gradleProperty("mavenCentralUsername")

val Project.mavenCentralPassword
  get() = providers.gradleProperty("mavenCentralPassword")

val Project.githubPackagesUsername
  get() = providers.gradleProperty("githubPackagesUsername")

val Project.githubPackagesPassword
  get() = providers.gradleProperty("githubPackagesPassword")

val Project.hasKmpPlugin
  get() = plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")

val Project.defaultJarManifest
  get() = buildMap {
    put("Enable-Native-Access", "ALL-UNNAMED")
    put("Built-By", System.getProperty("user.name"))
    put("Built-Jdk", System.getProperty("java.runtime.version"))
    put(
        "Built-OS",
        "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}")
    put("Build-Timestamp", DateTimeFormatter.ISO_INSTANT.format(ZonedDateTime.now()))
    put("Created-By", "Gradle ${gradle.gradleVersion}")
    put(Attributes.Name.IMPLEMENTATION_TITLE.toString(), project.name)
    put(Attributes.Name.IMPLEMENTATION_VERSION.toString(), project.version)
    put(Attributes.Name.IMPLEMENTATION_VENDOR.toString(), project.group)
    if (isAutomaticModuleEnabled) {
      put("Automatic-Module-Name", project.group)
    }
  }

val Project.containerLabels
  get() =
      mapOf(
          "maintainer" to project.githubUser,
          "org.opencontainers.image.authors" to project.githubUser,
          "org.opencontainers.image.title" to project.name,
          "org.opencontainers.image.description" to "🐳 ${project.description}",
          "org.opencontainers.image.version" to project.version.toString(),
          "org.opencontainers.image.vendor" to project.githubUser,
          "org.opencontainers.image.url" to project.githubRepo,
          "org.opencontainers.image.source" to project.githubRepo,
          "org.opencontainers.image.licenses" to "Apache-2.0")

val Project.defaultJvmArgs
  get() = buildList {
    addAll(libs.versions.java.jvmargs.get().split(",", " ").filter(String::isNotBlank))
    if (addModules.isNotBlank()) {
      add("--add-modules=$addModules")
    }
  }

/**
 * Runtime JVM arguments. If the user has provided a custom *jvmArgs*(`-PjvmArgs=...`) gradle
 * property, it will be used.
 * - [Java-Command](https://docs.oracle.com/en/java/javase/24/docs/specs/man/java.html)
 * - [Java-Networking-Parameters](https://docs.oracle.com/en/java/javase/24/core/java-networking.html#GUID-E6C82625-7C02-4AB3-B15D-0DF8A249CD73)
 * - [JSR166](https://cs.oswego.edu/dl/jsr166/dist/jsr166.jar)
 * - [Hotspot Options](https://chriswhocodes.com/hotspot_option_differences.html)
 * - [JFR Events](https://sap.github.io/SapMachine/jfrevents)
 * - [Profile.jfc](https://github.com/openjdk/jdk/blob/master/src/jdk.jfr/share/conf/jfr/profile.jfc)
 */
val Project.runJvmArgs
  get() = buildList {
    val appJvmArgs =
        extensions.findByType<JavaApplication>()?.applicationDefaultJvmArgs?.toList().orEmpty()
    when (appJvmArgs.isNotEmpty()) {
      true -> addAll(appJvmArgs)
      else -> {
        addAll(defaultJvmArgs)
        addAll(
            listOf(
                "--show-version",
                "-XX:+PrintCommandLineFlags",
                "--enable-native-access=ALL-UNNAMED",
                "--illegal-native-access=warn",
                "--sun-misc-unsafe-memory-access=warn",
                "-Xmx128M",
                "-XX:+UseZGC",
                "-XX:+UseStringDeduplication",
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+UseCompactObjectHeaders",
                "-XX:MaxRAMPercentage=0.8",
                // "-XX:+UseLargePages",
                // "-XX:+UseEpsilonGC",
                // "-XX:+AlwaysPreTouch",
                """-Xlog:gc*,stringdedup*:
                |file="$tmp$name-gc-%p-%t.log":
                |level,tags,time,uptime,pid,tid:
                |filecount=5,
                |filesize=10m"""
                    .joinToConfigString(),
                """-Xlog:class+load:file=$tmp$name-cds.log:
                |uptime,level,tags,pid:filesize=0"""
                    .joinToConfigString(),
                // java -XX:StartFlightRecording:help
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
                |+jdk.ObjectCount#enabled=true,
                |+jdk.SecurityPropertyModification#enabled=true,
                |+jdk.SecurityProviderService#enabled=true,
                |+jdk.TLSHandshake#enabled=true,
                |+jdk.TLSHandshake#stackTrace=true,
                |+jdk.X509Certificate#enabled=true,
                |+jdk.X509Validation#enabled=true,
                |+jdk.CPUTimeSample#enabled=true,
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
                // "-Djava.security.debug=properties",
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
                // "--sun-misc-unsafe-memory-access=warn",
                // "--finalization=enabled",
                // "-XX:OnOutOfMemoryError='kill -9 %p'",
                // "-ea",
                // "-XshowSettings:vm",
                // "-XshowSettings:system",
                // "-XshowSettings:properties",
                // "--show-module-resolution",
                // "-XX:-StackTraceInThrowable",
                // "-XX:+ShowHiddenFrames",
                // "-verbose:module",
                // "-XX:ConcGCThreads=2",
                // "-XX:VMOptionsFile=vm_options",
                // "-XX:+IgnoreUnrecognizedVMOptions",
                // "-XX:+StartAttachListener", // For jcmd Dynamic Attach Mechanism
                // "-XX:+DisableAttachMechanism",
                // "-XX:OnOutOfMemoryError="./restart.sh"",
                // "-XX:SelfDestructTimer=0.05",
                // "-XX:NativeMemoryTracking=[off|summary|detail]",
                // "-XX:+PrintNMTStatistics",
                // "-XX:OnError=\"gdb - %p\"", // Attach gdb on segfault
                // "-Djava.security.properties=/path/to/custom/java.security", // == to override
                // "-Duser.timezone=\"PST8PDT\"",
                // ----- Networking -----
                // "-Djdk.net.hosts.file=/etc/host/style/file",
                // "-Djava.net.preferIPv4Stack=true",
                // "-Djava.net.preferIPv6Addresses=true",
                // "-Djavax.net.debug=all",
                // "-Dhttps.protocols=TLSv1.3",
                // "-Dhttps.agent=$name",
                // "-Dhttp.keepAlive=true",
                // "-Dhttp.maxConnections=5",
                // ----- Java HTTP Client -----
                // "-Djdk.internal.httpclient.disableHostnameVerification",
                // "-Djdk.httpclient.HttpClient.log=errors,requests,headers",
                // "-Djdk.internal.httpclient.debug=false",
                // "-Djdk.tls.client.protocols=\"TLSv1.2,TLSv1.3\"",
                // "-Djdk.tls.maxCertificateChainLength=10",
                // "-Djdk.tls.maxHandshakeMessageSize=32768",
                // "-Djsse.enableSNIExtension=false",
                // "-Djdk.tls.client.enableCAExtension=true",
                // ----- Misc -----
                // "-Djava.security.manager=allow",
                // "-Dfile.encoding=COMPAT", // uses '-Dnative.encoding'
                // "-Djdbc.drivers=org.postgresql.Driver",
                // "-Djava.io.tmpdir=/var/data/tmp",
                // "-Djava.locale.providers=COMPAT,CLDR",
                // "-Djdk.lang.Process.launchMechanism=vfork",
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
      }
    }
  }

fun JavaToolchainSpec.configureJvmToolchain(project: Project) =
    with(project) {
      languageVersion = toolchainVersion
      nativeImageCapable = toolchainVendor.map { it.matches("graalvm community") }
      vendor = toolchainVendor
    }

fun JavaCompile.configureJavac(project: Project) =
    with(project) {
      options.apply {
        encoding = Charsets.UTF_8.name()
        release = javaRelease
        isIncremental = true
        isFork = true
        debugOptions.debugLevel = "source,lines,vars"
        forkOptions.jvmArgs?.addAll(defaultJvmArgs)
        compilerArgs.addAll(
            buildList {
              addAll(defaultJvmArgs)
              add("-Xlint:all")
              add("-parameters")
              // add("-Xlint:-deprecation")
              // add("-Xlint:lossy-conversions")
              // add("-XX:+IgnoreUnrecognizedVMOptions")
              // add("--add-exports")
              // add("java.base/sun.nio.ch=ALL-UNNAMED")
              // add("--patch-module")
              // add("$moduleName=${sourceSets.main.get().output.asPath}")
              // add("-Xplugin:unchecked") // compiler plugin
            })

        // Add the Kotlin classes to the module path (compileKotlinJvm)
        val compileKotlin = tasks.findByName("compileKotlin") as? KotlinCompile
        if (compileKotlin != null) {
          compilerArgumentProviders +=
              PatchModuleArgProvider(
                  provider { project.group.toString() }, compileKotlin.destinationDirectory)
        }
      }
    }

fun KotlinCommonCompilerOptions.configureKotlinCommon(project: Project) =
    with(project) {
      apiVersion = kotlinApiVersion
      languageVersion = kotlinLangVersion
      progressiveMode = true
      extraWarnings = false
      allWarningsAsErrors = false
      suppressWarnings = false
      verbose = false
      freeCompilerArgs.addAll(
          "-Xcontext-parameters",
          "-Xexpect-actual-classes",
          "-Xskip-prerelease-check",
          "-Xwhen-guards",
          "-Xmulti-dollar-interpolation",
          "-Xnon-local-break-continue",
          "-Xnested-type-aliases",
          "-Xreport-all-warnings",
          "-Xreturn-value-checker=check",
          // "-Wextra",
          // "-Xexpected-type-guided-resolution",
          // "-XXLanguage:+ExplicitBackingFields",
          // "-Xsuppress-version-warnings",
          // "-P",
          // "plugin:...=..."
      )
      optIn.addAll(
          "kotlin.ExperimentalStdlibApi",
          "kotlin.ExperimentalUnsignedTypes",
          "kotlin.contracts.ExperimentalContracts",
          "kotlin.io.encoding.ExperimentalEncodingApi",
          "kotlin.time.ExperimentalTime",
          "kotlinx.coroutines.ExperimentalCoroutinesApi",
          "kotlinx.serialization.ExperimentalSerializationApi",
          "kotlin.ExperimentalMultiplatform",
          "kotlin.js.ExperimentalJsExport",
          "kotlin.uuid.ExperimentalUuidApi",
          "kotlin.concurrent.atomics.ExperimentalAtomicApi"
          // "org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
          )
    }

fun KspAATask.configureKspConfig() =
    with(project) {
      kspConfig.apply {
        jvmTarget = kotlinJvmTarget.map { it.target }
        apiVersion = kotlinApiVersion.map { it.version }
        languageVersion = kotlinLangVersion.map { it.version }
        allWarningsAsErrors = false
      }
    }

/**
 * JVM backend compiler options can be found in,
 * - [CommonCompilerArgs](https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/CommonCompilerArguments.kt)
 * - [K2JVMCompilerArguments.kt](https://github.com/JetBrains/kotlin/blob/master/compiler/cli/cli-common/src/org/jetbrains/kotlin/cli/common/arguments/K2JVMCompilerArguments.kt)
 * - [JvmTarget.kt](https://github.com/JetBrains/kotlin/blob/master/compiler/config.jvm/src/org/jetbrains/kotlin/config/JvmTarget.kt)
 * - [ApiVersion.kt](https://github.com/JetBrains/kotlin/blob/master/compiler/util/src/org/jetbrains/kotlin/config/ApiVersion.kt#L35)
 *
 *   Gradle Kotlin DSL Api/Lang versions,
 * - [KotlinDslCompilerPlugins.kt](https://github.com/gradle/gradle/blob/master/platforms/core-configuration/kotlin-dsl-plugins/src/main/kotlin/org/gradle/kotlin/dsl/plugins/dsl/KotlinDslCompilerPlugins.kt#L63-L64)
 */
fun KotlinJvmCompilerOptions.configureKotlinJvm(project: Project) =
    with(project) {
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
          "-Xemit-jvm-type-annotations",
          "-Xjspecify-annotations=strict",
          "-Xskip-prerelease-check",
          // Remove null check intrinsics from bytecode
          "-Xno-param-assertions",
          "-Xno-call-assertions",
          "-Xno-receiver-assertions",
          // "-Xjdk-release=${kotlinJvmTarget.get().target}",
          // "-Xadd-modules=ALL-MODULE-PATH",
          // "-Xmodule-path=",
          // "-Xjvm-enable-preview",
          // "-Xjavac-arguments=\"--add-exports java.base/sun.nio.ch=ALL-UNNAMED\"",
          // "-Xexplicit-api={strict|warning|disable}",
          // "-Xgenerate-strict-metadata-version",
          // "-Xuse-kapt4",
      )
      // jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }

fun Test.configureJavaTest() {
  enabled = true
  useJUnitPlatform()
  jvmArgs(project.defaultJvmArgs)

  // For JUnit5 @EnabledIfSystemProperty
  systemProperty("ktorTest", project.hasProperty("ktorTest"))
  systemProperty("k8sTest", project.hasProperty("k8sTest"))
  systemProperty("spring.classformat.ignore", true)
  // Custom hosts file for tests
  val customHostFile = project.layout.projectDirectory.file("src/test/resources/hosts").asFile
  if (customHostFile.exists()) {
    systemProperty("jdk.net.hosts.file", customHostFile.absolutePath)
  }

  reports.html.required = true
  maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

  testLogging { configureLogEvents() }
  // timeout = 10.minutes.toJavaDuration()
  // filter { setExcludePatterns() }

  afterSuite(
      KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
        if (desc.parent == null) { // will match the outermost suite
          val status =
              when {
                result.failedTestCount > 0 -> "❌ FAILED"
                result.skippedTestCount == result.testCount -> "⚠️SKIPPED"
                else -> "✅ PASSED"
              }
          println(
              """
              |
              |Test Results: $status
              |━━━━━━━━━━━━━━━━━━━━━━━
              |Total    : ${result.testCount}
              |Passed   : ${result.successfulTestCount}
              |Failed   : ${result.failedTestCount}
              |Skipped  : ${result.skippedTestCount}
              |
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

fun KotlinJsCompilerOptions.configureKotlinJs() {
  freeCompilerArgs.addAll("-Xir-per-file")
  target = "es2015"
  // sourceMap = true
  // sourceMapEmbedSources = "always"
}

fun KotlinNpmInstallTask.configureKotlinNpm() {
  args.add("--ignore-engines")
}

/**
 * Adds a KSP dependency to the specified multiplatform sourceset.
 *
 * @param dependencyNotation The notation of the dependency to add.
 */
fun KotlinSourceSet.ksp(dependencyNotation: Any) {
  val kspConfiguration =
      when {
        name in
            listOf(
                KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME,
                KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME) -> "commonMainMetadata"

        name.endsWith("Main") -> name.substringBeforeLast("Main")
        else -> name
      }.replaceFirstChar { it.uppercaseChar() }
  project.dependencies.add("ksp$kspConfiguration", dependencyNotation)
}

/** Returns the path of the dependency jar in the runtime classpath. */
fun Project.depPathOf(dep: ExternalDependency): Provider<String> =
    configurations
        .named("runtimeClasspath")
        .flatMap { it.incoming.artifacts.resolvedArtifacts }
        .map {
          it.find { artifact ->
                when (val id = artifact.id) {
                  is ModuleComponentIdentifier -> id.module == dep.module.name
                  else -> false
                }
              }
              ?.file
              ?.path
        }

/** Returns the application `run` command. */
fun Project.appRunCmd(binary: Path, args: List<String>): String {
  val path = layout.projectDirectory.asFile.toPath().relativize(binary)
  val newLine = System.lineSeparator()
  val lineCont = """\""" // Bash line continuation
  val indent = "\t"
  return args.joinToString(
      prefix =
          """
             |To Run the app,
             |$ java -jar $lineCont $newLine
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
          else -> javaToolchainSvc?.launcherFor { configureJvmToolchain(project) }
        }?.orNull

    return jLauncher?.metadata?.installationPath?.asFile?.toPath()
        ?: error("Requested JDK version ($javaVersion) is not available.")
  }

/** Return incubator modules of the tool chain JDK */
val Project.incubatorModules
  get(): String {
    val javaCmd = project.javaToolchainPath.resolve("bin").resolve("java")
    val bos = ByteArrayOutputStream()
    val execResult =
        providers.exec {
          workingDir = layout.buildDirectory.get().asFile
          commandLine = listOf(javaCmd.toString())
          args = listOf("--list-modules")
          standardOutput = bos
          errorOutput = bos
        }
    execResult.result.get().assertNormalExitValue()
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

/**
 * Adds [file] as an outgoing variant to publication. Refer
 * [Customizing publishing](https://docs.gradle.org/current/userguide/publishing_customization.html)
 * for more details.
 */
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

/** Adds the give java module to all jvm tasks. Eg: `withModule("jdk.incubator.vector", false)` */
fun Project.withJavaModule(moduleName: String, supportedInNative: Boolean = false) =
    tasks.run {
      val argsToAdd = listOf("--add-modules", moduleName)
      withType<JavaCompile>().configureEach { options.compilerArgs.addAll(argsToAdd) }
      withType<Test>().configureEach { jvmArgs(argsToAdd) }
      withType<JavaExec>().configureEach { jvmArgs(argsToAdd) }
      if (supportedInNative) {
        project.pluginManager.withPlugin("org.graalvm.buildtools.native") {
          configure<GraalVMExtension> { binaries.all { jvmArgs(argsToAdd) } }
        }
      }
    }

fun Project.gradleBooleanProp(name: String): Provider<Boolean> =
    providers.gradleProperty(name).map(String::toBoolean).orElse(false)

inline fun <reified T> Project.extraProp(name: String, defaultValue: T): T =
    if (extra.has(name)) {
      extra[name] as? T ?: defaultValue
    } else defaultValue

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

/**
 * Generates the URL for the GitHub package repository based on the owner and repository name.
 *
 * @param owner The owner of the GitHub repository.
 * @param repository The name of the GitHub repository.
 * @return The URL of the GitHub package repository.
 */
fun githubPackage(owner: String, repository: String) =
    "https://maven.pkg.github.com/${owner.lowercase()}/$repository"

/**
 * Returns the latest download URL for a given [groupId] and [artifactId] from Maven Central.
 *
 * @param groupId the group ID of the Maven artifact
 * @param artifactId the artifact ID of the Maven artifact
 * @return the latest download URL for the specified Maven artifact
 */
fun mavenDownloadUrl(groupId: String, artifactId: String) =
    "https://search.maven.org/remote_content?g=${groupId}&a=${artifactId}&v=LATEST"
