package oblitusnumen.bondcalculator.impl

import oblitusnumen.bondcalculator.data.schema.CashFlowNormalized
import kotlin.math.pow


fun calculateInvestmentProfit(
    tax: Double,
    inflation: Double,
    rate: Double,
    initialSum: Double,
    monthlyDeposit: Double,
    t: Int,
): InvestmentProfitCalculateResult {
//    val tax = 13.0
//    val inflation = 7.0
//
//    val rate = 16.0
//    val initialSum = 710000.0
//    val monthlyDeposit = 15000.0
//    val t = 5

    val monthlyRate = (1 + rate * .01).pow(1.0 / 12)
    val tMonths = t * 12
    var s = initialSum

    repeat(tMonths) { s = s * monthlyRate + monthlyDeposit }

    val input = initialSum + tMonths * monthlyDeposit
    val profit = (s - input) * (1 - tax * .01)
    val clean = profit + input
    val inflationResult = clean / (1 + inflation * .01).pow(t)

    return InvestmentProfitCalculateResult(s, clean, profit, inflationResult)
}

data class InvestmentProfitCalculateResult(val totalReturn: Double, val cleanReturn: Double, val cleanProfit: Double, val purchasingPower: Double)

/**
 * Computes the final value of monthly deposits alone (initialSum = 0),
 * used as a helper constant in the reverse formula.
 */
private fun depositsGrowth(monthlyDeposit: Double, monthlyRate: Double, months: Int): Double {
    var s = 0.0
    repeat(months) { s = s * monthlyRate + monthlyDeposit }
    return s
}

/**
 * Reverse function: given a target purchasing power (inflationResult),
 * find the initialSum required to reach it.
 */
fun requiredInitialSum(
    tax: Double = 13.0,
    inflation: Double = 7.0,
    targetPurchasingPower: Double,
    monthlyDeposit: Double = 15000.0,
    years: Int = 5,
    rate: Double = 16.0
): Double {
    val monthlyRate = (1 + rate * .01).pow(1.0 / 12)
    val tMonths = years * 12

    val R = monthlyRate.pow(tMonths)
    val D = depositsGrowth(monthlyDeposit, monthlyRate, tMonths)
    val M = tMonths * monthlyDeposit
    val taxFactor = 1 - tax * .01
    val infFactor = (1 + inflation * .01).pow(years)

    val A = (R - 1) * taxFactor + 1
    val B = (D - M) * taxFactor + M

    return (targetPurchasingPower * infFactor - B) / A
}

/** Growth factor of a stream depositing exactly 1 per month (linear building block). */
private fun unitDepositGrowth(monthlyRate: Double, months: Int): Double {
    var s = 0.0
    repeat(months) { s = s * monthlyRate + 1.0 }
    return s
}

/**
 * Reverse function: given a target purchasing power (inflationResult) and a fixed
 * initialSum, find the monthlyDeposit required to reach it.
 */
fun requiredMonthlyDeposit(
    targetPurchasingPower: Double,
    initialSum: Double = 710000.0,
    years: Int = 5,
    rate: Double = 16.0,
    tax: Double = 13.0,
    inflation: Double = 7.0
): Double {
    val monthlyRate = (1 + rate * .01).pow(1.0 / 12)
    val tMonths = years * 12

    val R = monthlyRate.pow(tMonths)
    val K = unitDepositGrowth(monthlyRate, tMonths)
    val taxFactor = 1 - tax * .01
    val infFactor = (1 + inflation * .01).pow(years)

    val C = taxFactor * K + tMonths * (1 - taxFactor)
    val E = initialSum * (taxFactor * R + (1 - taxFactor))

    return (targetPurchasingPower * infFactor - E) / C
}

private const val MONTHS_PER_YEAR = 12.0

fun requiredRate(
    tax: Double = 13.0,
    targetPurchasingPower: Double,
    initialSum: Double = 710000.0,
    monthlyDeposit: Double = 15000.0,
    years: Int = 5,
    inflation: Double = 7.0,
    guessRate: Double = 0.1
): Double {
    val tMonths = years * MONTHS_PER_YEAR.toInt()
    val input = initialSum + tMonths * monthlyDeposit
    val taxFactor = 1 - tax * .01
    val infFactor = (1 + inflation * .01).pow(years)

    // Undo inflation, then tax, to find the required pre-tax final sum s.
    val clean = targetPurchasingPower * infFactor
    val profit = clean - input
    val s = profit / taxFactor + input

    val flows = buildList {
        add(CashFlowNormalized(-initialSum, 0.0))
        for (i in 1..tMonths) {
            add(CashFlowNormalized(-monthlyDeposit, i / MONTHS_PER_YEAR))
        }
        // last deposit and final value land on the same day, as separate flows
        add(CashFlowNormalized(s, tMonths / MONTHS_PER_YEAR))
    }

    val r = xirrByNofmalizedFlows(flows, guessRate = guessRate)
    return r * 100
}