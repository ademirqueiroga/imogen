/*
*  @author: Ademir Queiroga <admqueiroga@gmail.com>
*  @created: 27/03/21
*/

import annotations.*
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions

object ModelGenerator {

    private val defaultValueAnnotations = listOf(
        IntDefault::class.qualifiedName,
        StringDefault::class.qualifiedName,
        BooleanDefault::class.qualifiedName,
        Default::class.qualifiedName,
    )

    fun generate(modelData: ModelData): FileSpec {
        val classProperties = ArrayList<PropertySpec>()
        val primaryConstructorParameters = ArrayList<ParameterSpec>()
        val derivedProperties = ArrayList<ModelData.DerivedPropertyData>()
        for (propertyInfo in modelData.properties) {
            val classPropertySpecBuilder = PropertySpec.builder(propertyInfo.name, propertyInfo.type)
            classPropertySpecBuilder.addModifiers(KModifier.OVERRIDE)

            val primaryConstructorParameter = ParameterSpec.builder(propertyInfo.name, propertyInfo.type)
            var derivedProperty = false
            for (annotation in propertyInfo.annotations) {
                when (annotation.qualifiedName) {
                    in defaultValueAnnotations -> {
                        primaryConstructorParameter.processDefaultValueAnnotation(annotation)
                        if (propertyInfo.annotations.find { it.qualifiedName == Derived::class.qualifiedName } != null) {
                            throw ConflictingAnnotationsException(propertyInfo.name)
                        }
                    }
                    Derived::class.qualifiedName -> {
                        val deriveFrom = classPropertySpecBuilder.processDerivedAnnotation(annotation)
                        derivedProperties.add(ModelData.DerivedPropertyData(deriveFrom, propertyInfo))
                        derivedProperty = true
                    }
                    Create::class.qualifiedName -> {
                        classProperties.add(processCreateAnnotation(propertyInfo, annotation))
                    }
                    else -> {
                        classPropertySpecBuilder.passthroughUnprocessedAnnotation(annotation)
                    }
                }
            }

            if (!derivedProperty) {
                classPropertySpecBuilder.initializer(propertyInfo.name)
                primaryConstructorParameters.add(primaryConstructorParameter.build())
            }

            classProperties.add(classPropertySpecBuilder.build())
        }

        val paramName = modelData.superInterface.simpleName.substringAfterLast(".").decapitalize()
        val derivedPropertiesNames = derivedProperties.map { it.name }.asSequence()
        val secondaryConstructorArgs = modelData.properties.filter {
            it.name !in derivedPropertiesNames
        }.map {
            CodeBlock.of("\n${it.name} = ${paramName}.${it.name}")
        }
        val secondaryConstructor = FunSpec.constructorBuilder()
            .addParameter(paramName, modelData.superInterface)
            .callThisConstructor(secondaryConstructorArgs)
            .build()

        val primaryConstructor = FunSpec.constructorBuilder()
            .addParameters(primaryConstructorParameters)
            .build()

        val classSpec = TypeSpec.classBuilder(modelData.className)
            .addModifiers(KModifier.DATA)
            .addSuperinterface(modelData.superInterface)
            .primaryConstructor(primaryConstructor)
            .addProperties(classProperties)
            .addFunction(secondaryConstructor)
            .build()

        val fileBuilder = FileSpec.builder(
            modelData.packageName,
            ClassName(modelData.packageName, modelData.className).simpleName
        )

        val brandedProperties = modelData.brandedProperties
        if (brandedProperties != null && modelData.inheritedInterfaces.isNotEmpty()) {
            val delegateConstructorArg = brandedProperties.findArgument(Generate::delegatedClass.name)
            if (delegateConstructorArg?.value == true) {
                generateDelegatedConstructor(modelData, derivedProperties, fileBuilder)
            }
        }

        return fileBuilder.addType(classSpec).build()
    }

    private fun generateDelegatedConstructor(
        modelData: ModelData,
        derivedProperties: List<ModelData.DerivedPropertyData>,
        fileBuilder: FileSpec.Builder
    ) {
        val delegatedClassSpec = TypeSpec.classBuilder("Delegated${modelData.className}")
        delegatedClassSpec.addSuperinterface(modelData.superInterface)
        val delegatedClassPrimaryConstructor = FunSpec.constructorBuilder()
        modelData.inheritedInterfaces.forEach { className ->
            val propName = className.simpleName.substringAfterLast(".").decapitalize()
            delegatedClassPrimaryConstructor.addParameter(propName, className)
            delegatedClassSpec.addSuperinterface(className, CodeBlock.of(propName))
        }

        derivedProperties.forEach { derivedPropertyInfo ->
            val derivedPropertySpec = PropertySpec.builder(derivedPropertyInfo.name, derivedPropertyInfo.type)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(derivedPropertyInfo.deriveFrom)
            delegatedClassSpec.addProperty(derivedPropertySpec.build())
        }

        delegatedClassSpec.primaryConstructor(delegatedClassPrimaryConstructor.build())

        fileBuilder.addType(delegatedClassSpec.build())
    }

    private fun PropertySpec.Builder.passthroughUnprocessedAnnotation(annotation: ModelData.AnnotationData) {
        val annotationBuilder = AnnotationSpec.builder(ClassName.bestGuess(annotation.qualifiedName))
        for (arg in annotation.arguments) {
            val resolvedValue = when (val argValue = arg.value) {
                is Char -> "\'$argValue\'"
                is String -> argValue.escaped()
                is Float -> "${argValue}F"
                is Number -> argValue
                is Boolean -> argValue
                // This is the case where an annotation argument is a class reference or an Enum entry. [AQ]
                is KSType -> when (argValue.declaration.closestClassDeclaration()?.classKind) {
                    ClassKind.ENUM_ENTRY -> argValue.declaration.qualifiedName?.asString()
                    else -> "${argValue.declaration.qualifiedName?.asString()}::class"
                }
                null -> continue
                else -> TODO("Value type ${arg.value} not supported")
            }
            annotationBuilder.addMember(CodeBlock.of("${arg.name} = $resolvedValue"))
        }
        addAnnotation(annotationBuilder.build())
    }

    private fun ParameterSpec.Builder.processDefaultValueAnnotation(annotation: ModelData.AnnotationData) {
        val valueArgName = when (annotation.qualifiedName) {
            IntDefault::class.qualifiedName -> IntDefault::value.name
            StringDefault::class.qualifiedName -> StringDefault::value.name
            BooleanDefault::class.qualifiedName -> BooleanDefault::value.name
            Default::class.qualifiedName -> Default::provider.name
            else -> TODO("Default value type for ${annotation.qualifiedName} not supported")
        }
        val valueArg = annotation.arguments.first { it.name == valueArgName }
        val resolvedValue = when (val value = valueArg.value) {
            is String -> value.escaped()
            is Boolean,
            is Int -> value
            is KSType -> {
                val declaration = value.declaration
                check(declaration is KSClassDeclaration)
                if (declaration.classKind != ClassKind.OBJECT) {
                    throw NotAnObjectException(ValueProvider::class)
                }
                val functionName = ValueProvider::class.declaredFunctions.first().name
                CodeBlock.of("${declaration.qualifiedName!!.asString()}.$functionName()")
            }
            else -> TODO("Can't resolve value for $value yet")
        }
        defaultValue("$resolvedValue")
    }

    private fun PropertySpec.Builder.processDerivedAnnotation(annotation: ModelData.AnnotationData): String {
        val fromArg = annotation.findArgument(Derived::from.name)
        val fromArgValue = fromArg.value as String
        initializer(fromArgValue)
        return fromArgValue
    }

    private fun processCreateAnnotation(
        propertyInfo: ModelData.PropertyData,
        annotation: ModelData.AnnotationData,
    ): PropertySpec {
        val valueModifierArgument = annotation.findArgument(Create::valueModifier.name)
        val valueModifier = valueModifierArgument.value
        check(valueModifier is KSType)

        val valueModifierDeclaration = valueModifier.declaration
        check(valueModifierDeclaration is KSClassDeclaration)
        if (valueModifierDeclaration.classKind != ClassKind.OBJECT) {
            throw NotAnObjectException(ValueModifier::class)
        }

        val valueModifierElement = valueModifierDeclaration.superTypes.find { typeRef ->
            typeRef.resolve().declaration.qualifiedName!!.asString() == ValueModifier::class.qualifiedName
        }?.element
        check(valueModifierElement != null)

        val typeArguments = valueModifierElement.typeArguments

        // Verify if the input type argument from the provided modifier matches the property type. [AQ]
        val inputTypeArgumentDeclaration = typeArguments[0].type!!.resolve().declaration
        val inputTypeClassName = ClassName.bestGuess(inputTypeArgumentDeclaration.qualifiedName!!.asString())
        if (propertyInfo.type.toString() != inputTypeClassName.toString()) {
            throw UnsupportedTypeException(
                "Unsupported type ${propertyInfo.type} " +
                        "for property \"${propertyInfo.name}\" for ${valueModifierDeclaration.simpleName.asString()}"
            )
        }

        val outputTypeArgumentDeclaration = typeArguments[1].type!!.resolve().declaration
        val outputClassName = ClassName.bestGuess(outputTypeArgumentDeclaration.qualifiedName!!.asString())
        val propertyName = annotation.findArgument(Create::newProp.name).value
        val propertySpecBuilder = PropertySpec.builder(propertyName as String, outputClassName)
        val valueModifierClassName = valueModifierDeclaration.qualifiedName!!.asString()
        val modifyFunc = ValueModifier::class.declaredFunctions.first()
        propertySpecBuilder.initializer(
            CodeBlock.of("${valueModifierClassName}.${modifyFunc.name}(${propertyInfo.name})")
        )
        return propertySpecBuilder.build()
    }

    class ConflictingAnnotationsException(propertyName: String, vararg conflicts: ModelData.AnnotationData) :
        Exception(
            "Property $propertyName has conflicting annotations" +
                    " ${conflicts.joinToString { it.qualifiedName }}. Decide between which one to use"
        )

    class NotAnObjectException(klassName: KClass<*>) :
        Exception("Subclasses of $${klassName.simpleName} must be an `object`")

    class UnsupportedTypeException(message: String) : Exception(message)

}