package dev.suresh.system

import java.lang.classfile.ClassFile
import java.lang.classfile.ClassTransform
import java.lang.classfile.CodeElement
import java.lang.classfile.CodeTransform
import java.lang.classfile.MethodModel
import java.lang.classfile.Opcode
import java.lang.classfile.instruction.InvokeInstruction
import java.lang.constant.ClassDesc
import java.lang.constant.MethodTypeDesc
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.optionals.getOrNull

/**
 * An agent that blocks code from calling [System.exit]. The agent declares a premain method that is
 * run by the JVM before the main method of the application. This method registers a transformer
 * that transforms class files as they are loaded from the class path or module path. The
 * transformer rewrites every call to [System.exit] into throw new `RuntimeException("System.exit
 * not allowed")`.
 *
 * The transformer reads and writes bytecodes in class files using the Class-File API.
 *
 * This is directly ported from [JEP-486](https://openjdk.org/jeps/486#Appendix)
 */
class SystemExitTransformer : ClassFileTransformer {
  override fun transform(
      loader: ClassLoader?,
      className: String,
      classBeingRedefined: Class<*>,
      protectionDomain: ProtectionDomain,
      classBytes: ByteArray
  ): ByteArray? {
    return if (loader != ClassLoader.getPlatformClassLoader()) blockSystemExit(classBytes) else null
  }
}

fun blockSystemExit(classBytes: ByteArray): ByteArray? {
  val modified = AtomicBoolean(false)

  val cf = ClassFile.of(ClassFile.DebugElementsOption.DROP_DEBUG)
  val classModel = cf.parse(classBytes)

  val rewriteSystemExit = CodeTransform { codeBuilder, codeElement ->
    when (codeElement.isSystemExit()) {
      true -> {
        val rte = ClassDesc.of("java.lang.RuntimeException")
        codeBuilder
            .new_(rte)
            .dup()
            .ldc(java.lang.String("System.exit not allowed"))
            .invokespecial(
                rte, "<init>", MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)V"), false)
            .athrow()
        modified.set(true)
      }
      else -> codeBuilder.with(codeElement)
    }
  }

  val ct = ClassTransform.transformingMethodBodies({ it.hasSystemExit() }, rewriteSystemExit)
  val mod = cf.transformClass(classModel, ct)
  return if (modified.get()) mod else null
}

fun MethodModel.hasSystemExit() =
    code().getOrNull()?.elementStream()?.anyMatch { it.isSystemExit() } == true

fun CodeElement.isSystemExit() =
    when (this) {
      is InvokeInstruction ->
          opcode() == Opcode.INVOKESTATIC &&
              owner().asInternalName() == "java/lang/System" &&
              name().stringValue() == "exit" &&
              type().stringValue() == "(I)V"
      else -> false
    }
