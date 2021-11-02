package annotations

import kotlin.reflect.KClass

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