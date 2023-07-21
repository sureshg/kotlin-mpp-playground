import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import java.io.OutputStreamWriter

@AutoService(SymbolProcessorProvider::class)
class TestProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment) =
      TestProcessor(environment.codeGenerator, environment.logger)
}

class TestProcessor(val codeGenerator: CodeGenerator, val logger: KSPLogger) : SymbolProcessor {
  private var invoked = false

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val allFiles = resolver.getAllFiles().map { it.fileName }
    logger.warn(allFiles.toList().toString())
    if (invoked) {
      return emptyList()
    }
    invoked = true

    codeGenerator
        .createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = "",
            fileName = "Foo",
            extensionName = "kt")
        .use { output ->
          OutputStreamWriter(output).use { writer ->
            writer.write("package com.example\n\n")
            writer.write("class Foo {\n")

            val visitor = ClassVisitor()
            resolver.getAllFiles().forEach { it.accept(visitor, writer) }
            writer.write("}\n")
          }
        }
    return emptyList()
  }
}

class ClassVisitor : KSTopDownVisitor<OutputStreamWriter, Unit>() {
  override fun defaultHandler(node: KSNode, data: OutputStreamWriter) {}

  override fun visitClassDeclaration(
      classDeclaration: KSClassDeclaration,
      data: OutputStreamWriter
  ) {
    super.visitClassDeclaration(classDeclaration, data)
    val symbolName = classDeclaration.simpleName.asString().lowercase()
    data.write("    val $symbolName = true\n")
  }
}
