/*
*  @author: Ademir Queiroga <admqueiroga@gmail.com>
*  @created: 27/03/21
*/

package annotations

import kotlin.reflect.KClass


/**
 * Creates a new property in the generated class by modifying the
 * value of the annotated property using [ValueModifier].
 *
 * This is specifically useful when you don't want to pass a new
 * property in the constructor of the generated class but you still
 * want to have another property with a value based on an already
 * declared property.
 *
 * Example
 *
 * interface Example {
 *     @Create("newProp", PrefixWithClassNameModifier::class)
 *     val name: String
 * }
 *
 * The generated class will look like the following:
 *
 * class GeneratedExample(val name: String) {
 *     val newProp: String = PrefixWithClassNameModifier.modify(name)
 * }
 *
 * You might want to also take a look on [Derived]
 *
 */
@Repeatable
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class Create(
    val newProp: String,
    val valueModifier: KClass<out ValueModifier<*, *>>
)