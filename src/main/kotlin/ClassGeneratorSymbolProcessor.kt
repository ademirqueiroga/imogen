/*
*  @author: Ademir Queiroga <admqueiroga@gmail.com>
*  @created: 27/03/21
*/

import annotations.Generate
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*


class ClassGeneratorSymbolProcessor : SymbolProcessor {

    private lateinit var codeGenerator: CodeGenerator

    override fun init(
        options: Map<String, String>,
        kotlinVersion: KotlinVersion,
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ) {
        this.codeGenerator = codeGenerator
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Generate::class.qualifiedName!!)
        symbols.filterIsInstance<KSClassDeclaration>()
            .asSequence()
            .forEach { symbol ->
                if (symbol.classKind != ClassKind.INTERFACE) {
                    throw NotAnInterfaceException()
                }
                val packageName = symbol.packageName.asString()
                val modelData = ModelData.create(packageName, symbol)
                ModelGenerator.generate(modelData).writeTo(codeGenerator)
            }
        return emptyList()
    }

    class NotAnInterfaceException : Exception("The annotated class must be an interface")

}