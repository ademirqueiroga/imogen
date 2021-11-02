package annotations


@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Generate(
    val name: String,
    val prefix: String = "",
    val suffix: String = "",
)
