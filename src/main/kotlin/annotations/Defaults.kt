/*
*  @author: Ademir Queiroga <admqueiroga@gmail.com>
*  @created: 27/03/21
*/

package annotations

import kotlin.reflect.KClass


/**
 * Allows a property of an interface to receive the
 * default [Int] [value] when the implementation is being
 * generated
 *
 * @param value the value to assign to the property
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class IntDefault(val value: Int)

/**
 * Allows a property of an interface to receive the
 * default [String] [value] when the implementation is being
 * generated
 *
 * @param value the value to assign to the property
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class StringDefault(val value: String)

/**
 * Allows a property of an interface to receive the
 * default [Boolean] [value] when the implementation is being
 * generated
 *
 * @param value the value to assign to the property
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class BooleanDefault(val value: Boolean)

/**
 * Allows a property of an interface to receive the
 * value from the [ValueProvider] [provider] when the
 * implementation is being generated
 *
 * @param provider the provider which will be called
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Default(val provider: KClass<out ValueProvider<*>>)