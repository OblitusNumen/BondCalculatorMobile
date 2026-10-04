package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import oblitusnumen.bondcalculator.impl.calculateReturn
import oblitusnumen.bondcalculator.impl.calculateYearlyPercentage
import java.time.LocalDate
import kotlin.math.roundToInt

@Serializable
data class LocalBond(
    val id: Int,
    val name: String,
    val bondValue: Double,
    val bondMaturityEpochDay: Long,
    val couponPeriodDays: Int,
    val couponValue: Double,
    val bondPricePrcnt: Double,
    val accruedOffset: Int,
) {
    val maturityDate: LocalDate
        get() = LocalDate.ofEpochDay(bondMaturityEpochDay)

    val bondPrice: Double
        get() = bondPricePrcnt * bondValue / 100

    fun getInvestmentPeriod(now: LocalDate): Int = (maturityDate.toEpochDay() - now.toEpochDay()).toInt()

    fun getCouponCount(investmentPeriod: Int): Int =
        if (couponPeriodDays == 0) 0 else (investmentPeriod - 1) / couponPeriodDays + 1

    fun getAccruedEsteem(investmentPeriod: Int): Double = if (couponPeriodDays == 0) 0.0 else
        couponValue * (couponPeriodDays - ((((investmentPeriod - accruedOffset - 2) % couponPeriodDays) + couponPeriodDays) % couponPeriodDays) - 1) / couponPeriodDays.toDouble()

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
        val accrued = getAccruedEsteem(investmentPeriod)
        val bondCost = getBondCost(buyCommission, accrued)
        val totalCouponValue = getTotalCouponValue(couponCount)
        val couponRate = getNominalCouponRate(settings)
        val bondization = getBondization(settings, couponCount)
        val totalReturn = bondValue + totalCouponValue
        val totalProfit = totalReturn - bondCost
        val totalProfitRatePercentage = calculateYearlyPercentage(settings, investmentPeriod, bondCost, totalReturn)
        val cleanProfit = totalProfit * (1 - tax)
        val cleanReturn = bondCost + cleanProfit
        val cleanProfitRatePercentage = calculateYearlyPercentage(settings, investmentPeriod, bondCost, cleanReturn)
        val commission = this@LocalBond.bondPrice * buyCommission
        val cleanCoupon = couponValue * (1 - tax)
        val priceRatePercentage = calculateYearlyPercentage(settings, investmentPeriod,
            this@LocalBond.bondPrice, bondValue)

        val bondReinvestProfit =
            calculateBondReinvestProfit(
                settings,
                priceRatePercentage * .01,
                couponCount,
                cleanCoupon,
                buyCommission,
                tax
            )
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
            this@LocalBond.bondPrice,
            accrued,
            getAccruedEsteem(0),
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
            bondization,
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
            val newBondCost = newBondPrice * (1 + buyCommission) + getAccruedEsteem(0)
            reinvestBondsCount += newCoupon / newBondCost
            reinvestCost += newCoupon
        }

        val reinvestReturn = (bondValue + couponValue) * reinvestBondsCount
        val reinvestTotalProfit = reinvestReturn - reinvestCost

        return reinvestReturn - reinvestTotalProfit * tax - cleanCoupon * (couponCount - 1)
    }

    fun getBondization(settings: FinanceParameters, couponCount: Int): List<BondPayment> {
        val result = mutableListOf<BondPayment>()

        val nominalCouponRate = getNominalCouponRate(settings)
        repeat(couponCount) { idx ->
            result.add(BondPayment(BondPaymentType.Amortization, bondMaturityEpochDay, bondValue, 100.0))
            result.add(
                BondPayment(
                    BondPaymentType.Coupon,
                    maturityDate.minusDays((couponPeriodDays * (couponCount - idx - 1)).toLong()).toEpochDay(),
                    couponValue,
                    nominalCouponRate
                )
            )
        }

        return result
    }

    private fun getNominalCouponRate(settings: FinanceParameters): Double =
        couponValue * settings.daysInYear / couponPeriodDays / bondValue * 100

    private fun getTotalCouponValue(couponCount: Int): Double = couponValue * couponCount

    private fun getBondCost(buyCommission: Double, accrued: Double): Double = this@LocalBond.bondPrice * (1 + buyCommission) + accrued

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

    fun withBondMaturityDate(bondMaturity: LocalDate): LocalBond = copy(
        bondMaturityEpochDay = bondMaturity.toEpochDay()
    )

    override fun toString(): String = Json.encodeToString(serializer(), this)

    companion object {
        fun fromString(string: String?): LocalBond? {
            return if (string.isNullOrEmpty())
                null
            else
                Json.decodeFromString(serializer(), string)
        }

        fun getAccruedOffset(
            accruedDate: LocalDate,
            accruedInt: Double,
            couponValue: Double,
            matDate: LocalDate,
            couponPeriod: Int
        ): Int {
            if (couponPeriod == 0 || couponValue == 0.0) return 0
            val duration = (matDate.toEpochDay() - accruedDate.toEpochDay()).toInt()
            val accruedEsteem =
                couponValue * (couponPeriod - ((((duration - 2) % couponPeriod) + couponPeriod) % couponPeriod) - 1) / couponPeriod.toDouble()
            return ((accruedInt - accruedEsteem) * couponPeriod / couponValue).roundToInt()
        }
    }

    data class CalculateResult(
        val tax: Double,
        val commissionPercentage: Double,
        val depositReinvestmentRatePercentage: Double,
        val numberOfLots: Int,
        val investmentDate: LocalDate,
        val investmentPeriod: Int,
        val investmentCost: Double,
        val bondPrice: Double,
        val accrued: Double,
        val reinvestAccrued: Double,
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
        val bondization: List<BondPayment>,
        val couponCount: Int,
        val totalCouponValue: Double,
    ) {
        fun withLots(number: Int): CalculateResult {
            val ratio = number / numberOfLots.toDouble()
            return copy(
                numberOfLots = number,
                investmentCost = investmentCost * ratio,
                bondPrice = bondPrice * ratio,
                accrued = accrued * ratio,
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
                bondization = bondization.map { it * ratio },
                totalCouponValue = totalCouponValue * ratio,
            )
        }
    }
}