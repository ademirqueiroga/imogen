/*
*  @author: Ademir Queiroga <admqueiroga@gmail.com>
*  @created: 27/03/21
*/

package annotations

fun interface ValueModifier<in Input, out Output> {

    fun modify(source: Input): Output

}