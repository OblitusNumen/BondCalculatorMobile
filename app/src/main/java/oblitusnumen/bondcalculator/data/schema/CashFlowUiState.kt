package oblitusnumen.bondcalculator.data.schema

import androidx.compose.ui.focus.FocusState
import kotlin.math.min

data class CashFlowUiState(
    val amount: Double,
    val amountStr: String,
    val dateEpochDay: Long
) {
    fun cashFlow(): CashFlow = CashFlow(amount, dateEpochDay)
    fun withAmountString(amountStr: String): CashFlowUiState {
        var newAmount = amount
        var amountStr = amountStr.replace(',', '.')
        amountStr.indexOf('.')
            .let { if (it != -1) amountStr = amountStr.substring(0, min(amountStr.length, it + 3)) }
        try {
            newAmount = amountStr.toDouble()
        } catch (_: Exception) {
        }
        return copy(amount = newAmount, amountStr = amountStr)
    }

    fun resetStr(): CashFlowUiState =
        copy()
}