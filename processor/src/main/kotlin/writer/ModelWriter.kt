package writer

import value.ModelData
import annotations.ValueProvider
import annotations.*
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import escaped
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions

object ModelWriter {

    private val defaultValueAnnotations = listOf(
        BooleanDefault::class.qualifiedName,
        ByteDefault::class.qualifiedName,
        CharDefault::class.qualifiedName,
        Default::class.qualifiedName,
        DoubleDefault::class.qualifiedName,
        FloatDefault::class.qualifiedName,
        IntDefault::class.qualifiedName,
        LongDefault::class.qualifiedName,
        ShortDefault::class.qualifiedName,
        StringDefault::class.qualifiedName,
    )


    fun writeSpec(modelData: ModelData): FileSpec {
        val classProperties = ArrayList<PropertySpec>(modelData.properties.size)
        val primaryConstructorParameters = ArrayList<ParameterSpec>()

        for (propertyInfo in modelData.properties) {
            val classPropertySpecBuilder = PropertySpec.builder(propertyInfo.name, propertyInfo.type)
            classPropertySpecBuilder.addModifiers(KModifier.OVERRIDE)

            val primaryConstructorParameter = ParameterSpec.builder(propertyInfo.name, propertyInfo.type)
            for (annotation in propertyInfo.annotations) {
                if (annotation.qualifiedName in defaultValueAnnotations) {
                    primaryConstructorParameter.processDefaultValueAnnotation(annotation)
                } else {
                    classPropertySpecBuilder.passthroughUnprocessedAnnotation(annotation)
                }
            }

            classPropertySpecBuilder.initializer(propertyInfo.name)
            primaryConstructorParameters.add(primaryConstructorParameter.build())

            classProperties.add(classPropertySpecBuilder.build())
        }

        val paramName = modelData.superInterface.simpleName.substringAfterLast(".")
            .replaceFirstChar { it.lowercase(Locale.getDefault()) }

        val secondaryConstructorArgs = modelData.properties.map {
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

        return fileBuilder.addType(classSpec).build()
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
            BooleanDefault::class.qualifiedName -> BooleanDefault::value.name
            ByteDefault::class.qualifiedName -> ByteDefault::value.name
            CharDefault::class.qualifiedName -> CharDefault::value.name
            DoubleDefault::class.qualifiedName -> DoubleDefault::value.name
            FloatDefault::class.qualifiedName -> FloatDefault::value.name
            IntDefault::class.qualifiedName -> IntDefault::value.name
            LongDefault::class.qualifiedName -> LongDefault::value.name
            ShortDefault::class.qualifiedName -> ShortDefault::value.name
            StringDefault::class.qualifiedName -> StringDefault::value.name
            Default::class.qualifiedName -> Default::provider.name
            else -> return
        }
        val valueArg = annotation.arguments.first { it.name == valueArgName }
        val resolvedValue = when (val value = valueArg.value) {
            is String -> value.escaped()
            is Boolean -> value
            is Number -> value
            is KSType -> {
                val declaration = value.declaration
                check(declaration is KSClassDeclaration)
                if (declaration.classKind != ClassKind.OBJECT) {
                    // TODO: Improve logging
                    throw NotAnObjectException(ValueProvider::class)
                }
                val functionName = ValueProvider::class.declaredFunctions.first().name
                CodeBlock.of("${declaration.qualifiedName!!.asString()}.$functionName()")
            }
            else -> TODO("Can't resolve value for $value yet")
        }
        defaultValue("$resolvedValue")
    }

    class NotAnObjectException(klassName: KClass<*>) :
        Exception("Subclasses of $${klassName.simpleName} must be an `object`")

}