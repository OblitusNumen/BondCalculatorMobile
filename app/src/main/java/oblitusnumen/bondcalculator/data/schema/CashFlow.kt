package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.Serializable
import oblitusnumen.bondcalculator.ui.formatDouble
import java.time.LocalDate

/**
 * Cash flow with a calendar date (time of day is ignored).
 */
@Serializable
data class CashFlow(
    val amount: Double,
    val dateEpochDay: Long
) {
    val date: LocalDate
        get() = LocalDate.ofEpochDay(dateEpochDay)

    fun cashFlowUiState(): CashFlowUiState = CashFlowUiState(amount, formatDouble(amount, 2), dateEpochDay)

    operator fun times(multiplier: Double): CashFlow = copy(amount = amount * multiplier)
}