package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import oblitusnumen.bondcalculator.impl.calculateReturn
import oblitusnumen.bondcalculator.impl.calculateYearlyPercentage
import oblitusnumen.bondcalculator.impl.xirr
import java.time.LocalDate

data class BondDetails(
    val bond: Bond,
    val marketData: MarketData,
//    val yield: MarketDataYield,
    val dataVersion: DataVersion
) {
    fun changePrcAccurate() =
        marketData.last?.times(100.0)?.div(bond.prevPrice ?: 100.0)?.minus(100.0)

    fun toLocalBond(id: Int = 0): LocalBond {
        return LocalBond(
            id,
            bond.shortname!!,
            bond.faceValue!!,
            maturityDate?.toEpochDay() ?: LocalDate.now().toEpochDay(),
            bond.couponPeriod!!,
            bond.couponValue!!,
            bondPricePrcnt,
            getAccruedOffset()
        )
    }

    fun getAccruedOffset(): Int = LocalBond.getAccruedOffset(
        LocalDate.parse(dataVersion.tradeSessionDate),
        bond.accruedInt ?: 0.0,
        bond.couponValue!!,
        maturityDate ?: LocalDate.now(),
        bond.couponPeriod!!
    )

    fun getAccruedEsteem(investmentPeriod: Int, accruedOffset: Int): Double {
        val couponPeriodDays = bond.couponPeriod ?: 0
        return if (couponPeriodDays == 0)
            0.0
        else
            bond.couponValue!! * (couponPeriodDays - ((((investmentPeriod - accruedOffset - 2) % couponPeriodDays) + couponPeriodDays) % couponPeriodDays) - 1) / couponPeriodDays.toDouble()
    }

    val maturityDate: LocalDate?
        get() =
            try {
                LocalDate.parse(bond.matDate)
            } catch (_: Exception) {
                null
            }

    val bondPricePrcnt
        get() = marketData.last ?: 100.0

    fun withPrice(pricePrcnt: Double): BondDetails = copy(marketData = marketData.copy(last = pricePrcnt))

    val bondPrice: Double
        get() = bondPricePrcnt * bond.faceValue!! / 100

    fun getBondPrice(faceValue: Double): Double = bondPricePrcnt * faceValue / 100

    fun getInvestmentPeriod(nowEpochDay: Long): Int =
        ((maturityDate?.toEpochDay() ?: LocalDate.now().toEpochDay()) - nowEpochDay).toInt()

    fun getBondCost(faceValue: Double, buyCommission: Double, accrued: Double): Double =
        getBondPrice(faceValue) * (1 + buyCommission) + accrued

    fun calculateProfit(
        fullBondization: List<BondPayment>,
        settings: FinanceParameters,
        hasCommission: Boolean,
        isTaxed: Boolean,
        investmentDate: LocalDate,
        numberOfLots: Int,
    ): CalculateResult {
        val buyCommission = if (hasCommission) settings.commissionRate else 0.0
        val tax = if (isTaxed) settings.taxPercentage * .01 else 0.0
        var faceValue = bond.faceValue!!
        val tradeSessionEpochDay = LocalDate.parse(dataVersion.tradeSessionDate).toEpochDay()
        val investmentEpochDay = investmentDate.toEpochDay()
        val diff = (investmentEpochDay - tradeSessionEpochDay).toInt()
        if (diff != 0) {
            fullBondization.filter { it.type == BondPaymentType.Amortization }.forEach {
                if (diff > 0 && tradeSessionEpochDay < it.epochDay && it.epochDay <= investmentEpochDay)
                    faceValue -= it.value
                else if (diff > 0 && investmentEpochDay < it.epochDay && it.epochDay <= tradeSessionEpochDay)
                    faceValue += it.value
            }
        }
        val bondPrice = getBondPrice(faceValue)
        val bondization = fullBondization.filter { it.epochDay > investmentEpochDay }
        val coupons = bondization.filter { it.type == BondPaymentType.Coupon }
        val totalCouponValue = coupons.sumOf { it.value }
        val couponRate = coupons.lastOrNull()?.valuePercent ?: 0.0

        if (maturityDate == null) {
            val couponEpochDay = bondization.lastCoupon()?.epochDay ?: investmentEpochDay
            val investmentPeriod: Int = (couponEpochDay - investmentEpochDay).toInt()
            getAccruedEsteem(investmentPeriod, getAccruedOffset())
            val accruedOffset = LocalBond.getAccruedOffset(
                LocalDate.ofEpochDay(tradeSessionEpochDay),
                bond.accruedInt ?: 0.0,
                bond.couponValue!!,
                LocalDate.ofEpochDay(couponEpochDay),
                bond.couponPeriod!!
            )
            val couponPeriodDays = bond.couponPeriod
            val accrued = if (couponPeriodDays == 0)
                0.0
            else
                bond.couponValue * (couponPeriodDays - ((((investmentPeriod - accruedOffset - 2) % couponPeriodDays) + couponPeriodDays) % couponPeriodDays) - 1) / couponPeriodDays.toDouble()
            val bondCost = getBondCost(faceValue, buyCommission, accrued)
            val commission = bondPrice * buyCommission

            val totalProfitRatePercentage = couponRate / bondCost * faceValue
            val cleanProfitRatePercentage = couponRate * (1 - tax) / bondCost * faceValue
            val sellPrice = getBondPrice(faceValue) * (1 - buyCommission)/* + accrued*/
            val couponValue = bondization.lastCoupon()?.value ?: 0.0
            val cleanCoupon = couponValue * (1 - tax)
            val effectiveReturn: Double = cleanCoupon + sellPrice - (sellPrice - bondCost) * tax
            val effectiveProfitRatePercentage: Double = calculateYearlyPercentage(
                settings,
                investmentPeriod,
                bondCost,
                effectiveReturn
            )

            return CalculateResult(
                tax * 100,
                buyCommission * 100,
                settings.bondReinvestThruDepositPercentage,
                1,
                investmentDate,
                investmentPeriod,
                bondCost,
                bondPrice,
                accrued,
                commission,
                0.0,
                0.0,
                couponValue,
                totalProfitRatePercentage,
                0.0,
                cleanCoupon,
                cleanProfitRatePercentage,
                effectiveReturn,
                effectiveReturn - bondCost,
                effectiveProfitRatePercentage,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                Double.NaN,
                couponRate,
                bondization.reversed(),
                coupons.size,
                totalCouponValue,
                faceValue
            ).withLots(numberOfLots)
        }

        val investmentPeriod: Int = getInvestmentPeriod(investmentEpochDay)
        val accrued = getAccruedEsteem(investmentPeriod, getAccruedOffset())
        val bondCost = getBondCost(faceValue, buyCommission, accrued)
        val totalReturn = bondization.sumOf { it.value }
        val totalProfit = totalReturn - bondCost
        val totalProfitRatePercentage = calculateYearlyPercentage(settings, investmentPeriod, bondCost, totalReturn)
        val cleanProfit = totalProfit * (1 - tax)
        val cleanReturn = bondCost + cleanProfit
        val cleanProfitRatePercentage = calculateYearlyPercentage(settings, investmentPeriod, bondCost, cleanReturn)
        val commission = bondPrice * buyCommission

        val priceRatePercentage = xirr(mutableListOf<CashFlow>().apply xirr@{
            add(CashFlow(-bondCost, investmentEpochDay))
            addAll(bondization.filter { it.type == BondPaymentType.Amortization }.map { it.cashFlow })
            println(StringBuilder().apply {
                this@xirr.forEach {
                    append("${it.amount}:${it.date}")
                    append("\n")
                }
            }.toString())
        }) * 100

        var effectiveBondProfit = 0.0
        val effectiveBondProfitPercentage: Double = xirr(mutableListOf<CashFlow>().apply xirr@{
            add(CashFlow(-bondCost, investmentEpochDay))
            addAll(bondization.map {
                CashFlow(
                    if (it.type == BondPaymentType.Coupon)
                        it.value * (1 - tax)
                    else
                        it.value * (1 - (1 - bondCost / faceValue) * tax),
                    it.epochDay
                )
            })
            println(StringBuilder().apply {
                append("effectiveBondProfitPercentage\n")
                this@xirr.forEach {
                    append("${it.amount}:${it.date}")
                    append("\n")
                }
            }.toString())
            effectiveBondProfit = sumOf { it.amount }
        }) * 100

        var depositEffectiveProfit = 0.0
        val depositEffectiveProfitRatePercentage: Double = xirr(mutableListOf<CashFlow>().apply xirr@{
            add(CashFlow(-bondCost, investmentEpochDay))
            addAll(bondization.map {
                CashFlow(
                    calculateReturn(
                        settings,
                        ((maturityDate?.toEpochDay() ?: LocalDate.now().toEpochDay()) - it.epochDay).toInt(),
                        if (it.type == BondPaymentType.Coupon)
                            it.value * (1 - tax)
                        else
                            it.value * (1 - (1 - bondCost / faceValue) * tax),
                        settings.bondReinvestThruDepositPercentage / 100
                    ), maturityDate?.toEpochDay() ?: LocalDate.now().toEpochDay()
                )
            })
            println(StringBuilder().apply {
                append("depositEffectiveProfitRatePercentage\n")
                this@xirr.forEach {
                    append("${it.amount}:${it.date}")
                    append("\n")
                }
            }.toString())
            depositEffectiveProfit = sumOf { it.amount }
        }) * 100
        val depositReinvestProfit: Double = depositEffectiveProfit - effectiveBondProfit
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
            accrued,
            commission,
            priceRatePercentage,
            totalReturn,
            totalProfit,
            totalProfitRatePercentage,
            cleanReturn,
            cleanProfit,
            cleanProfitRatePercentage,
            effectiveBondProfit + bondCost,
            effectiveBondProfit,
            effectiveBondProfitPercentage,
            depositEffectiveProfit + bondCost,
            depositReinvestProfit,
            depositEffectiveProfit,
            depositEffectiveProfitReinvestPercentage,
            depositEffectiveProfitRatePercentage,
            couponRate,
            bondization.reversed(),
            coupons.size,
            totalCouponValue,
            faceValue
        ).withLots(numberOfLots)
    }

    companion object {
        fun fromJson(root: JsonObject): List<BondDetails> {
            //bond
            val bonds = root["securities"]?.jsonObject ?: throw IllegalStateException("No 'securities' block")
            val bondColArray = bonds["columns"]?.jsonArray ?: throw IllegalStateException("No 'columns' array")
            val bondColList = bondColArray.map { it.jsonPrimitive.content }
            val bondDataArray = bonds["data"]?.jsonArray ?: emptyList()

            //market data
            val marketData = root["marketdata"]?.jsonObject ?: throw IllegalStateException("No 'marketdata' block")
            val marketDataColArray =
                marketData["columns"]?.jsonArray ?: throw IllegalStateException("No 'columns' array")
            marketDataColArray.map { it.jsonPrimitive.content }
            val marketDataMap: MutableMap<Pair<String, String?>, MarketData> = mutableMapOf()
            (marketData["data"]?.jsonArray ?: emptyList()).forEach { row ->
                MarketData.fromRow(row.jsonArray.map { it.jsonPrimitive.content })
                    .apply { marketDataMap[secid to boardid] = this }
            }

//            //yields data
//            val yieldsData =
//                root["marketdata_yields"]?.jsonObject ?: throw IllegalStateException("No 'marketdata_yields' block")
//            val yieldsDataColArray =
//                yieldsData["columns"]?.jsonArray ?: throw IllegalStateException("No 'columns' array")
//            yieldsDataColArray.map { it.jsonPrimitive.content }
//            val yieldsDataMap: MutableMap<Pair<String, String?>, MarketDataYield> = mutableMapOf()
//            (yieldsData["data"]?.jsonArray ?: emptyList()).forEach { row ->
//                MarketDataYield.fromRow(row.jsonArray.map { it.jsonPrimitive.content })
//                    .apply { yieldsDataMap[secid to boardid] = this }
//            }

            //data version
            val versionData =
                root["dataversion"]?.jsonObject ?: throw IllegalStateException("No 'dataversion' block")
            versionData["columns"]?.jsonArray ?: throw IllegalStateException("No 'columns' array")

            val result: MutableList<BondDetails> = mutableListOf()

            repeat(bondDataArray.size) { index ->
                val bondValues = bondDataArray[index].jsonArray.map { it.jsonPrimitive.content }
                Bond.fromMap(bondColList.zip(bondValues).toMap()).apply {
                    result.add(
                        BondDetails(
                            this,
                            marketDataMap[secid to boardid] ?: MarketData(secid),
//                            yieldsDataMap[secid to boardid] ?: MarketDataYield(secid),
                            DataVersion.fromRow(versionData["data"]?.jsonArray?.firstOrNull()?.jsonArray?.map { it.jsonPrimitive.content }
                                ?: Array<String?>(4) { null }.toList())
                        )
                    )
                }
            }

            return result
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
        val buyCommission: Double,
        val priceRatePercentage: Double,

        val totalReturn: Double,
        val totalProfit: Double,
        val totalProfitRatePercentage: Double,

        val cleanReturn: Double,
        val cleanProfit: Double,
        val cleanProfitRatePercentage: Double,

        val effectiveReturn: Double,
        val effectiveProfit: Double,
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
        val faceValue: Double,
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
                effectiveProfit = effectiveProfit * ratio,

                depositEffectiveReturn = depositEffectiveReturn * ratio,
                depositReinvestProfit = depositReinvestProfit * ratio,
                depositEffectiveProfit = depositEffectiveProfit * ratio,
                bondization = bondization.map { it * ratio },
                totalCouponValue = totalCouponValue * ratio,
            )
        }

        fun toLocalCalculateResult(): LocalBond.CalculateResult =
            LocalBond.CalculateResult(
                tax = tax,
                commissionPercentage = commissionPercentage,
                depositReinvestmentRatePercentage = depositReinvestmentRatePercentage,
                numberOfLots = numberOfLots,
                investmentDate = investmentDate,
                investmentPeriod = investmentPeriod,
                investmentCost = investmentCost,
                bondPrice = bondPrice,
                accrued = accrued,
                reinvestAccrued = Double.NaN,
                buyCommission = buyCommission,
                priceRatePercentage = priceRatePercentage,
                totalReturn = totalReturn,
                totalProfit = totalProfit,
                totalProfitRatePercentage = totalProfitRatePercentage,
                cleanReturn = cleanReturn,
                cleanProfit = cleanProfit,
                cleanProfitRatePercentage = cleanProfitRatePercentage,
                effectiveReturn = effectiveReturn,
                reinvestProfit = Double.NaN,
                effectiveProfit = effectiveProfit,
                effectiveProfitReinvestPercentage = Double.NaN,
                effectiveProfitRatePercentage = effectiveProfitRatePercentage,
                depositEffectiveReturn = depositEffectiveReturn,
                depositReinvestProfit = depositReinvestProfit,
                depositEffectiveProfit = depositEffectiveProfit,
                depositEffectiveProfitReinvestPercentage = depositEffectiveProfitReinvestPercentage,
                depositEffectiveProfitRatePercentage = depositEffectiveProfitRatePercentage,
                nominalCouponRate = nominalCouponRate,
                bondization = bondization,
                couponCount = couponCount,
                totalCouponValue = totalCouponValue
            )
    }
}