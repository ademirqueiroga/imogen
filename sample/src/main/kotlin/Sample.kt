import annotations.Generate

/*
 *  @author: Ademir Queiroga <admqueiroga@gmail.com>
 *  @created: 02/11/21
 */

@Generate(name = "SampleImpl")
interface Sample {
    val string: String
    val int: Int
    val long: Long
    val double: Double
    val float: Float
    val sample: Sample
}