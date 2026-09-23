package oblitusnumen.bondcalculator.data.schema

data class CashFlowNormalized(
    val amount: Double,
    val years: Double          // fractional years from the earliest cash flow
)