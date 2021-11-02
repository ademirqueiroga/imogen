package annotations

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