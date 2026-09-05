package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.Serializable

/**
 * Cash flow with a calendar date (time of day is ignored).
 */
@Serializable
data class CashFlow(
    val amount: Double,
    val dateEpochDay: Long
)