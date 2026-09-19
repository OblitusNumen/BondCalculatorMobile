package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.Serializable
import oblitusnumen.bondcalculator.ui.formatDouble

/**
 * Cash flow with a calendar date (time of day is ignored).
 */
@Serializable
data class CashFlow(
    val amount: Double,
    val dateEpochDay: Long
) {
    fun cashFlowUiState(): CashFlowUiState = CashFlowUiState(amount, formatDouble(amount, 2), dateEpochDay)
}