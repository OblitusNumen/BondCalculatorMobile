package oblitusnumen.bondcalc

data class MoexInstrument(
    val name: String,
    val engine: String,
    val market: String,
    val board: String,
    val security: String
)