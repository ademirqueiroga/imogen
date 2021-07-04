/*
*  @author: Ademir Queiroga <admqueiroga@gmail.com>
*  @created: 27/03/21
*/

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

@Suppress("NOTHING_TO_INLINE")
inline fun String.escaped() = "\"$this\""

fun KSAnnotation.findArgument(argName: String) = arguments.firstOrNull {
    it.name?.asString() == argName
}

fun ModelData.AnnotationData.findArgument(argName: String) =
    this.arguments.first { it.name == argName }

inline fun <reified T> KSClassDeclaration.findAnnotation(): KSAnnotation? = annotations.find { annotation ->
    val qualifiedName = annotation.annotationType.resolve().declaration.qualifiedName?.asString()
    qualifiedName == T::class.qualifiedName
}

internal fun FileSpec.writeTo(codeGenerator: CodeGenerator) {
    val dependencies = Dependencies(true, *originatingKSFiles().toTypedArray())
    val file = codeGenerator.createNewFile(dependencies, packageName, name)
    // Don't use writeTo(file) because that tries to handle directories under the hood
    OutputStreamWriter(file, StandardCharsets.UTF_8).use(::writeTo)
}