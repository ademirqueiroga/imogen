package value

import annotations.Generate
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import findAnnotation
import findArgument


data class ModelData(
    val packageName: String,
    val className: String,
    val properties: List<PropertyData>,
    val superInterface: ClassName,
    val inheritedInterfaces: List<ClassName>,
    val generateAnnotation: KSAnnotation?,
) {

    open class PropertyData(
        val name: String,
        val type: TypeName,
        val annotations: List<AnnotationData>
    )

    class AnnotationData(
        val qualifiedName: String,
        val arguments: List<Argument>
    ) {

        data class Argument(val name: String, val value: Any?)

    }

    companion object {

        private fun KSClassDeclaration.extractPropertiesInfo(): Sequence<PropertyData> {
            return getAllProperties().map { property ->
                val propType = property.type.resolve()
                val propDeclaration = propType.declaration
                var propClassName: TypeName = ClassName.bestGuess(
                    (propDeclaration.qualifiedName ?: propDeclaration.simpleName).asString()
                )

                if (propDeclaration.typeParameters.isNotEmpty()) {
                    propClassName = (propClassName as ClassName).parameterizedBy(
                        propType.arguments.map { arg ->
                            ClassName.bestGuess(arg.type?.resolve()?.declaration?.qualifiedName?.asString()!!)
                        }
                    )
                }

                PropertyData(
                    name = property.simpleName.asString(),
                    type = propClassName.copy(propType.isMarkedNullable),
                    annotations = property.extractPropertyAnnotations()
                )
            }
        }

        private fun KSPropertyDeclaration.extractPropertyAnnotations(): List<AnnotationData> {
            return annotations.map { annotation ->
                val declaration = annotation.annotationType.resolve().declaration
                val qualifiedName = (declaration.qualifiedName ?: declaration.simpleName).asString()
                val annotationArguments = ArrayList<AnnotationData.Argument>()
                for (argument in annotation.arguments) {
                    val argName = argument.name?.asString()!!
                    annotationArguments.add(AnnotationData.Argument(argName, argument.value))
                }
                AnnotationData(qualifiedName, annotationArguments)
            }.toList()
        }

        private fun KSClassDeclaration.extractInterfacesClassNames(): List<ClassName> {
            val classNames = ArrayList<ClassName>()
            val directlyInheritedInterfaces = superTypes.filter { ksTypeReference ->
                val declaration = ksTypeReference.resolve().declaration as KSClassDeclaration
                declaration.classKind == ClassKind.INTERFACE
            }

            classNames += directlyInheritedInterfaces.map {
                val declaration = it.resolve().declaration
                ClassName("", (declaration.qualifiedName ?: declaration.simpleName).asString())
            }

            return classNames
        }

        fun create(packageName: String, symbol: KSClassDeclaration): ModelData {
            val generateAnnotation = symbol.findAnnotation<Generate>()
            val prefix = generateAnnotation?.findArgument(Generate::prefix.name)?.value ?: ""
            val suffix = generateAnnotation?.findArgument(Generate::suffix.name)?.value ?: ""
            val name = generateAnnotation?.findArgument(Generate::name.name)?.value?.toString()
            if (name.isNullOrEmpty()) {
                throw InvalidClassNameException(symbol)
            }
            val superClass = ClassName(packageName, (symbol.qualifiedName ?: symbol.simpleName).asString())
            return ModelData(
                packageName = packageName,
                className = "$prefix$name$suffix",
                properties = symbol.extractPropertiesInfo().toList(),
                superInterface = superClass,
                inheritedInterfaces = symbol.extractInterfacesClassNames(),
                generateAnnotation = generateAnnotation
            )
        }

    }

    // TODO: Improve exception message
    class InvalidClassNameException(val symbol: KSClassDeclaration) :
        Exception("Invalid value for generated class name. Make sure the name parameter is not empty and is different from inherited classes")

}