/*
*  @author: Ademir Queiroga <admqueiroga@gmail.com>
*  @created: 27/03/21
*/

package annotations

/**
 * Allows a property of an interface to have it's own
 * value to be derived to another property of the interface.
 *
 * This is specifically useful when you don't want to pass
 * the Derived property in the constructor of the generated
 * class but you still want to have a different property
 * where you can annotate with something to be processed
 * later on.
 *
 * Example:
 *
 * interface Example {
 *
 *     val y: String
 *
 *     @Derived(from = "y")
 *     val x: String
 * }
 *
 * The generated class will look like the following:
 *
 * class GeneratedExample(val y: String) {
 *   val x: String = y
 * }
 *
 * If you have a runtime annotation also in the property "x"
 * this one can be later processed and do something with the
 * value of "y" without modifying it.
 *
 * You might want to also take a look on [Create]
 *
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Derived(val from: String)