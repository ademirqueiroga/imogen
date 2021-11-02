package annotations

/*
 *  @author: Ademir Queiroga <admqueiroga@gmail.com>
 *  @created: 02/11/21
 */

/**
 * Allows a property of an interface to receive the
 * default [Long] [value] when the implementation is being
 * generated
 *
 * @param value the value to assign to the property
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class LongDefault(val value: Long)