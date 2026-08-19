package oblitusnumen.bondcalculator.impl

import java.time.LocalDate
import kotlin.math.pow

data class ProfitCalculationResult(
    val period: Int,
    val buyCost: Double,
    val totalReturn: Double,
    val cleanReturn: Double,
    val cleanProfitPercentage: Double,
    val cleanProfit: Double,
    val taxValue: Double
)

fun calculateDepositProfit(
    settings: FinanceParameters,
    ratePercentage: Double,
    input: Double,
    periodDays: Int
): ProfitCalculationResult {
    return calculateProfit(
        settings,
        false,
        input,
        input * (1 + ratePercentage * .01 * periodDays / settings.daysInYear),
        LocalDate.now(),
        LocalDate.now().plusDays(periodDays.toLong()),
    )
}

fun calculateProfit(
    settings: FinanceParameters,
    isTaxed: Boolean,
    buyCost: Double,
    totalReturn: Double,
    buyDate: LocalDate,
    sellDate: LocalDate
): ProfitCalculationResult {
    val tax = if (isTaxed) settings.taxPercentage * .01 else 0.0
    val period: Int = (sellDate.toEpochDay() - buyDate.toEpochDay()).toInt()
    val profit = totalReturn - buyCost
    val cleanProfit = profit * (1 - tax)
    val cleanReturn = buyCost + cleanProfit
    val cleanProfitPercentage = calculateYearlyPercentage(settings, period, buyCost, cleanReturn)

    return ProfitCalculationResult(
        period,
        buyCost,
        totalReturn,
        cleanReturn,
        cleanProfitPercentage,
        cleanProfit,
        profit * tax
    )
}

fun calculateYearlyPercentage(settings: FinanceParameters, periodDays: Int, input: Double, totalReturn: Double) =
    ((totalReturn / input).pow(settings.daysInYear / periodDays.toDouble()) - 1) * 100

fun calculateReturn(settings: FinanceParameters, periodDays: Int, input: Double, rate: Double) =
    input * (rate + 1).pow(periodDays / settings.daysInYear.toDouble())