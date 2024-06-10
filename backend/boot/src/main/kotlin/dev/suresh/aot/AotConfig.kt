package dev.suresh.aot

import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportRuntimeHints

@Configuration @ImportRuntimeHints(Hints::class) class AotConfig

class Hints : RuntimeHintsRegistrar {
  override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
    hints.reflection().apply {
      // registerType(Klass::class.java, *MemberCategory.entries.toTypedArray())
      // registerType(Klass::class.java, MemberCategory.INVOKE_DECLARED_METHODS)
    }
  }
}
