package processor

import value.ModelData
import writer.ModelWriter
import annotations.Generate
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import writeTo


class ImogenSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Generate::class.qualifiedName!!)
        symbols.filterIsInstance<KSClassDeclaration>()
            .asSequence()
            .forEach { symbol ->
                if (symbol.classKind != ClassKind.INTERFACE) {
                    logger.error(NotAnInterfaceException.message!!, symbol)
                } else {
                    val packageName = symbol.packageName.asString()
                    try {
                        val modelData = ModelData.create(packageName, symbol)
                        if (modelData.className == modelData.superInterface.simpleName) {
                            logger.error(RedeclarationException.message!!, symbol)
                        } else {
                            ModelWriter.writeSpec(modelData).writeTo(codeGenerator)
                        }
                    } catch (e: ModelData.InvalidClassNameException) {
                        logger.error(e.message!!, e.symbol)
                    }
                }
            }
        return emptyList()
    }

    object NotAnInterfaceException: Exception("The annotated class must be an interface")
    object RedeclarationException: Exception("The name of the generated class must be unique")

}