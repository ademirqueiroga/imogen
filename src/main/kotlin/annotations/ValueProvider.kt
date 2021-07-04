package annotations

/*
 *  @author: Ademir Queiroga <admqueiroga@gmail.com>
 *  @created: 05/04/21
 */

fun interface ValueProvider<out Output> {

    fun provide(): Output

}