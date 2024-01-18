package common

object Platform {

  enum class OS(val id: String) {
    Linux("linux"),
    MacOS("macos"),
    Windows("windows"),
    Unknown("unknown")
  }

  enum class Arch(val isa: String) {
    X64("x64"),
    Arm64("aarch64"),
    Unknown("unknown")
  }

  data class Target(
      val os: OS,
      val version: String,
      val arch: Arch,
  ) {
    val id
      get() = "${os.id}-${arch.isa}"
  }

  val currentOS by lazy {
    val os = System.getProperty("os.name")
    when {
      os.startsWith("Mac", ignoreCase = true) -> OS.MacOS
      os.startsWith("Win", ignoreCase = true) -> OS.Windows
      os.startsWith("Linux", ignoreCase = true) -> OS.Linux
      else -> error("Unsupported OS: $os")
    }
  }

  val currentArch by lazy {
    when (val osArch = System.getProperty("os.arch")) {
      "x86_64",
      "amd64" -> Arch.X64
      "aarch64",
      "arm64" -> Arch.Arm64
      else -> error("Unsupported OS arch: $osArch")
    }
  }

  val currentTarget by lazy {
    Target(os = currentOS, version = System.getProperty("os.version"), arch = currentArch)
  }

  val isWin
    get() = currentOS == OS.Windows

  val isMac
    get() = currentOS == OS.MacOS

  val isLinux
    get() = currentOS == OS.Linux

  val isUnix
    get() = isMac || isLinux

  val isAarch64
    get() = currentArch == Arch.Arm64

  val isAmd64
    get() = currentArch == Arch.X64

  val isCygwin
    get() = isWin && System.getenv("PWD") != null && System.getenv("PWD").startsWith("/")

  val isMSystem
    get() =
        isWin &&
            System.getenv("MSYSTEM") != null &&
            (System.getenv("MSYSTEM").startsWith("MINGW") || System.getenv("MSYSTEM") == "MSYS")

  val isWSL
    get() = System.getenv("WSL_DISTRO_NAME") != null

  val isWSL1
    get() = isWSL && System.getenv("WSL_INTEROP") == null

  val isWSL2
    get() = isWSL && isWSL1.not()
}
