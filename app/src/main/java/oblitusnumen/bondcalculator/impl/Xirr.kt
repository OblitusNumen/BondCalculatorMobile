package oblitusnumen.bondcalculator.impl

import oblitusnumen.bondcalculator.data.schema.CashFlow
import kotlin.math.abs
import kotlin.math.pow


private data class CashFlowNormalized(
    val amount: Double,
    val years: Double          // fractional years from the earliest cash flow
)

private fun calculateResult(flows: List<CashFlowNormalized>, ratePlusOne: Double): Double {
    var result = 0.0
    for (flow in flows) {
        result += flow.amount / ratePlusOne.pow(flow.years)
    }
    return result
}

private fun calculateResultDerivation(flows: List<CashFlowNormalized>, ratePlusOne: Double): Double {
    var result = 0.0
    for (flow in flows) {
        result -= (flow.years * flow.amount) / ratePlusOne.pow(flow.years + 1.0)
    }
    return result
}

private fun normalize(flows: List<CashFlow>): List<CashFlowNormalized> {
    require(flows.isNotEmpty()) { "Cash flows must not be empty" }

    val sorted = flows.sortedBy { it.dateEpochDay }
    val firstDate = sorted.first().dateEpochDay

    return sorted.map { flow ->
        val days = flow.dateEpochDay - firstDate
        CashFlowNormalized(
            amount = flow.amount,
            years = days / 365.0          // same day-count convention as the original (ACT/365)
        )
    }
}

/**
 * Calculates XIRR (same algorithm as LibreOffice / Excel / the original TypeScript library).
 */
fun xirr(
    flows: List<CashFlow>,
    guessRate: Double = 0.1,
    maxEpsilon: Double = .0001,
    maxScans: Int = 200,
    maxIterations: Int = 20
): Double {
    if (flows.isEmpty())
        return 0.0

    val normalized = normalize(flows)

    if (normalized.none { it.amount > 0 }) {
        throw IllegalArgumentException("No positive amount was found in cash flows")
    }
    if (normalized.none { it.amount < 0 }) {
        throw IllegalArgumentException("No negative amount was found in cash flows")
    }
    if (guessRate <= -1.0) {
        throw IllegalArgumentException("Guess rate is less than or equal to -1")
    }
    if (maxEpsilon <= 0.0) {
        throw IllegalArgumentException("Max epsilon is less than or equal to 0")
    }
    if (maxScans < 10) {
        throw IllegalArgumentException("Max scans is lower than 10")
    }
    if (maxIterations < 10) {
        throw IllegalArgumentException("Max iterations is lower than 10")
    }

    val firstAmount = normalized[0].amount
    val remaining = normalized.drop(1)

    var resultRate = guessRate
    var resultValue: Double
    var iterationScan = 0
    var doLoop: Boolean

    do {
        if (iterationScan >= 1) {
            resultRate = -0.99 + (iterationScan - 1) * 0.01
        }

        var iteration = maxIterations
        do {
            val ratePlusOne = resultRate + 1.0
            resultValue = firstAmount + calculateResult(remaining, ratePlusOne)

            val derivation = calculateResultDerivation(remaining, ratePlusOne)
            val newRate = resultRate - resultValue / derivation

            val rateEpsilon = abs(newRate - resultRate)
            resultRate = newRate

            doLoop = rateEpsilon > maxEpsilon && abs(resultValue) > maxEpsilon
        } while (doLoop && --iteration > 0)

        doLoop = doLoop ||
                resultRate.isNaN() || !resultRate.isFinite() ||
                resultValue.isNaN() || !resultValue.isFinite()

    } while (doLoop && ++iterationScan < maxScans)

    if (doLoop) {
        throw IllegalStateException(
            "XIRR calculation failed. Try increasing maxEpsilon, maxScans or maxIterations"
        )
    }

    return resultRate
}