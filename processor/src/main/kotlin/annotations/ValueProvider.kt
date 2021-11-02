package annotations

fun interface ValueProvider<out Output> {

    fun provide(): Output

}