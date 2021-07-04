/*
*  @author: Ademir Queiroga <admqueiroga@gmail.com>
*  @created: 27/03/21
*/

import annotations.Generate
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName


data class ModelData constructor(
    val packageName: String,
    val className: String,
    val properties: List<PropertyData>,
    val superInterface: ClassName,
    val inheritedInterfaces: List<ClassName>,
    val brandedProperties: KSAnnotation?,
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

    class DerivedPropertyData(
        val deriveFrom: String,
        name: String,
        type: TypeName,
        annotations: List<AnnotationData>
    ) : PropertyData(name, type, annotations) {

        constructor(deriveFrom: String, propertyData: PropertyData) : this(
            deriveFrom, propertyData.name, propertyData.type, propertyData.annotations
        )

    }

    companion object {

        private const val CLASS_NAME_SUFFIX = "BrandedProperties"

        private fun KSClassDeclaration.extractPropertiesInfo(): List<PropertyData> {
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
            val data = ArrayList<AnnotationData>()
            for (annotation in annotations) {
                val declaration = annotation.annotationType.resolve().declaration
                val qualifiedName = (declaration.qualifiedName ?: declaration.simpleName).asString()
                val annotationArguments = ArrayList<AnnotationData.Argument>()
                for (argument in annotation.arguments) {
                    val argName = argument.name?.asString()!!
                    annotationArguments.add(AnnotationData.Argument(argName, argument.value))
                }
                data.add(AnnotationData(qualifiedName, annotationArguments))
            }
            return data
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
            val brandedProperties = symbol.findAnnotation<Generate>()
            val brandedPropertiesName = brandedProperties?.findArgument(Generate::name.name)?.value?.toString()
            val className = if (!brandedPropertiesName.isNullOrEmpty()) brandedPropertiesName else {
                "${symbol.simpleName.asString()}$CLASS_NAME_SUFFIX"
            }
            val superClass = ClassName(packageName, (symbol.qualifiedName ?: symbol.simpleName).asString())
            return ModelData(
                packageName = packageName,
                className = className,
                properties = symbol.extractPropertiesInfo(),
                superInterface = superClass,
                inheritedInterfaces = symbol.extractInterfacesClassNames(),
                brandedProperties = brandedProperties
            )
        }

    }

}