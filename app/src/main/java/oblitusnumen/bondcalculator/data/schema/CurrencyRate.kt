package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class CurrencyRate(
    val id: String,
    val charCode: String,
    val numCode: String,
    val nominal: Int,
    val name: String,
    val value: Double,
    val vUnitRate: Double,
    val dateEpochDay: Long
) {
    val date
        get() = LocalDate.ofEpochDay(dateEpochDay)
}