package annotations

/*
 *  @author: Ademir Queiroga <admqueiroga@gmail.com>
 *  @created: 02/11/21
 */

/**
 * Allows a property of an interface to receive the
 * default [Short] [value] when the implementation is being
 * generated
 *
 * @param value the value to assign to the property
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ShortDefault(val value: Short)
