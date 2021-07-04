/*
*  @author: Ademir Queiroga <admqueiroga@gmail.com>
*  @created: 27/03/21
*/

package annotations


/**
 * @param delegatedClass tells if a class for a delegated constructor should be generated.
 *
 *  A delegate class is a class where the implementation of its inherited interfaces
 *  is delegated to the parameters received in it's constructor.
 *
 *  Example:
 *
 *  interface Bundle : Config, Styles, Settings
 *
 *  @Generate
 *  class Properties(...) : Bundle {
 *      ...
 *  }
 *
 * The generated class will look like the following:
 *
 *  class DelegatedProperties(
 *      config: Config,
 *      styles: Styles,
 *      settings: Settings,
 *  ): Bundle,
 *     Config by config,
 *     Styles by styles,
 *     Settings by settings {
 *  }
 *
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Generate(
    val name: String = CLASS_NAME,
    val delegatedClass: Boolean = false,
) {

    companion object {
        const val CLASS_NAME = ""
    }

}
