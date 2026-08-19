package oblitusnumen.bondcalculator.impl

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

@Serializable
data class Bond(
    val id: Int,
    val name: String = "",
    val bondValue: Double = DEFAULT_BOND_VALUE,
    val bondReturnDateYear: Int = LocalDate.now().plusDays(1).year,
    val bondReturnDateMonth: Int = LocalDate.now().plusDays(1).monthValue,
    val bondReturnDateDay: Int = LocalDate.now().plusDays(1).dayOfMonth,
    val couponPeriodDays: Int = DEFAULT_COUPON_PERIOD,
    val couponValue: Double = 0.0,
    val bondPrice: Double = bondValue,
    val nkdOffset: Int = 0,
) {
    val bondReturnDate: LocalDate
        get() = LocalDate.of(bondReturnDateYear, bondReturnDateMonth, bondReturnDateDay)

    fun getInvestmentPeriod(now: LocalDate): Int = (bondReturnDate.toEpochDay() - now.toEpochDay()).toInt()

    fun getCouponCount(investmentPeriod: Int): Int = (investmentPeriod - 1) / couponPeriodDays + 1

    fun getNkdEsteem(investmentPeriod: Int): Double =
        couponValue * (couponPeriodDays - ((((investmentPeriod - nkdOffset - 2) % couponPeriodDays) + couponPeriodDays) % couponPeriodDays) - 1) / couponPeriodDays.toDouble()

    fun calculateProfit(
        settings: FinanceParameters,
        hasCommission: Boolean,
        isTaxed: Boolean,
        investmentDate: LocalDate,
        numberOfLots: Int,
    ): CalculateResult {
        val buyCommission = if (hasCommission) settings.commissionRate else 0.0
        val tax = if (isTaxed) settings.taxPercentage * .01 else 0.0

        val investmentPeriod: Int = getInvestmentPeriod(investmentDate)
        val couponCount = getCouponCount(investmentPeriod)
        val nkd = getNkdEsteem(investmentPeriod)
        val bondCost = getBondCost(buyCommission, nkd)
        val totalCouponValue = getTotalCouponValue(couponCount)
        val couponRate = getNominalCouponRate(settings)
        val coupons = getCoupons(couponCount)
        val totalReturn = bondValue + totalCouponValue
        val totalProfit = totalReturn - bondCost
        val totalProfitRatePercentage = calculateYearlyPercentage(settings, investmentPeriod, bondCost, totalReturn)
        val cleanProfit = totalProfit * (1 - tax)
        val cleanReturn = bondCost + cleanProfit
        val cleanProfitRatePercentage = calculateYearlyPercentage(settings, investmentPeriod, bondCost, cleanReturn)
        val commission = bondPrice * buyCommission
        val cleanCoupon = couponValue * (1 - tax)
        val priceRatePercentage = calculateYearlyPercentage(settings, investmentPeriod, bondPrice, bondValue)

        val bondReinvestProfit =
            calculateBondReinvestProfit(settings, priceRatePercentage * .01, couponCount, cleanCoupon, buyCommission, tax)
        val effectiveBondProfit: Double = bondReinvestProfit + cleanProfit
        val effectiveBondProfitPercentage: Double =
            calculateYearlyPercentage(settings, investmentPeriod, bondCost, effectiveBondProfit + bondCost)
        val bondReinvestPercentage: Double = bondReinvestProfit / effectiveBondProfit * 100

        val depositReinvestProfit = calculateDepositReinvestProfit(settings, couponCount, couponPeriodDays, cleanCoupon)
        val depositEffectiveProfit: Double = depositReinvestProfit + cleanProfit
        val depositEffectiveProfitRatePercentage: Double =
            calculateYearlyPercentage(settings, investmentPeriod, bondCost, depositEffectiveProfit + bondCost)
        val depositEffectiveProfitReinvestPercentage: Double = depositReinvestProfit / depositEffectiveProfit * 100


        return CalculateResult(
            tax * 100,
            buyCommission * 100,
            settings.bondReinvestThruDepositPercentage,
            1,
            investmentDate,
            investmentPeriod,
            bondCost,
            bondPrice,
            nkd,
            getNkdEsteem(0),
            commission,
            priceRatePercentage,
            totalReturn,
            totalProfit,
            totalProfitRatePercentage,
            cleanReturn,
            cleanProfit,
            cleanProfitRatePercentage,
            effectiveBondProfit + bondCost,
            bondReinvestProfit,
            effectiveBondProfit,
            bondReinvestPercentage,
            effectiveBondProfitPercentage,
            depositEffectiveProfit + bondCost,
            depositReinvestProfit,
            depositEffectiveProfit,
            depositEffectiveProfitReinvestPercentage,
            depositEffectiveProfitRatePercentage,
            couponRate,
            coupons,
            couponCount,
            totalCouponValue,
        ).withLots(numberOfLots)
    }

    fun calculateBondReinvestProfit(
        settings: FinanceParameters,
        priceRate: Double,
        couponCount: Int,
        cleanCoupon: Double,
        buyCommission: Double,
        tax: Double
    ): Double {
        var reinvestBondsCount = 0.0
        var reinvestCost = 0.0
        repeat(couponCount - 1) {
            val newCoupon = reinvestBondsCount * cleanCoupon + cleanCoupon
            val newBondPrice =
                bondValue / calculateReturn(settings, couponPeriodDays * (couponCount - 1 - it), 1.0, priceRate)
            //            val newBondPrice = bondValue - (bondValue - bondPrice) / investmentPeriod * (couponPeriodDays * (couponCount - 1 - it))
            val newBondCost = newBondPrice * (1 + buyCommission) + getNkdEsteem(0)
            reinvestBondsCount += newCoupon / newBondCost
            reinvestCost += newCoupon
        }

        val reinvestReturn = (bondValue + couponValue) * reinvestBondsCount
        val reinvestTotalProfit = reinvestReturn - reinvestCost

        return reinvestReturn - reinvestTotalProfit * tax - cleanCoupon * (couponCount - 1)
    }

    fun getCoupons(couponCount: Int): List<Coupon> {
        val result = mutableListOf<Coupon>()

        repeat(couponCount) { idx ->
            result.add(Coupon(bondReturnDate.minusDays((couponPeriodDays * (couponCount - idx - 1)).toLong()), couponValue))
        }

        return result
    }

    private fun getNominalCouponRate(settings: FinanceParameters): Double =
        couponValue * settings.daysInYear / couponPeriodDays / bondValue * 100

    private fun getTotalCouponValue(couponCount: Int): Double = couponValue * couponCount

    private fun getBondCost(buyCommission: Double, nkd: Double): Double = bondPrice * (1 + buyCommission) + nkd

    fun calculateDepositReinvestProfit(
        settings: FinanceParameters,
        couponCount: Int,
        couponPeriodDays: Int,
        cleanCoupon: Double
    ): Double {
        var cumulativeReinvestReturn = 0.0

        repeat(couponCount) {
            cumulativeReinvestReturn =
                cumulativeReinvestReturn * (1 + (settings.bondReinvestThruDepositPercentage * .01 * couponPeriodDays / settings.daysInYear)) + cleanCoupon
        }

        return cumulativeReinvestReturn - cleanCoupon * couponCount
    }

    fun withBondReturnDate(bondReturnDate: LocalDate): Bond = copy(
        bondReturnDateYear = bondReturnDate.year,
        bondReturnDateMonth = bondReturnDate.monthValue,
        bondReturnDateDay = bondReturnDate.dayOfMonth
    )

    override fun toString(): String = Json.encodeToString(serializer(), this)

    companion object {
        fun fromString(string: String?): Bond? {
            return if (string.isNullOrEmpty())
                null
            else
                Json.decodeFromString(serializer(), string)
        }
    }

    data class Coupon(val date: LocalDate, val value: Double) {
        operator fun times(ratio: Double): Coupon = copy(value = value * ratio)
    }

    data class CalculateResult(
        val tax: Double,
        val commissionPercentage: Double,
        val depositReinvestmentRatePercentage: Double,
        val numberOfLots: Int,
        val investmentDate: LocalDate,
        val investmentPeriod: Int,
        val investmentCost: Double,
        val bondsCost: Double,
        val nkd: Double,
        val reinvestNkd: Double,
        val buyCommission: Double,
        val priceRatePercentage: Double,

        val totalReturn: Double,
        val totalProfit: Double,
        val totalProfitRatePercentage: Double,

        val cleanReturn: Double,
        val cleanProfit: Double,
        val cleanProfitRatePercentage: Double,

        val effectiveReturn: Double,
        val reinvestProfit: Double,
        val effectiveProfit: Double,
        val effectiveProfitReinvestPercentage: Double,
        val effectiveProfitRatePercentage: Double,

        val depositEffectiveReturn: Double,
        val depositReinvestProfit: Double,
        val depositEffectiveProfit: Double,
        val depositEffectiveProfitReinvestPercentage: Double,
        val depositEffectiveProfitRatePercentage: Double,

        val nominalCouponRate: Double,
        val coupons: List<Coupon>,
        val couponCount: Int,
        val totalCouponValue: Double,
    ) {
        fun withLots(number: Int): CalculateResult {
            val ratio = number / numberOfLots.toDouble()
            return copy(
                numberOfLots = number,
                investmentCost = investmentCost * ratio,
                bondsCost = bondsCost * ratio,
                nkd = nkd * ratio,
                buyCommission = buyCommission * ratio,

                totalReturn = totalReturn * ratio,
                totalProfit = totalProfit * ratio,

                cleanReturn = cleanReturn * ratio,
                cleanProfit = cleanProfit * ratio,

                effectiveReturn = effectiveReturn * ratio,
                reinvestProfit = reinvestProfit * ratio,
                effectiveProfit = effectiveProfit * ratio,

                depositEffectiveReturn = depositEffectiveReturn * ratio,
                depositReinvestProfit = depositReinvestProfit * ratio,
                depositEffectiveProfit = depositEffectiveProfit * ratio,
                coupons = coupons.map { it * ratio },
                totalCouponValue = totalCouponValue * ratio,
            )
        }
    }
}