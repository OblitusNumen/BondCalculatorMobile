package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.time.LocalDate

fun List<BondPayment>?.lastCoupon(): BondPayment? = this?.lastOrNull { it.isCoupon }

fun List<BondPayment>?.lastNotNullCoupon(): BondPayment? = this?.lastOrNull { it.isCoupon && it.hasValue }

fun List<BondPayment>?.firstCoupon(): BondPayment? = this?.firstOrNull { it.isCoupon }

fun List<BondPayment>?.lastNotNullAmortization(): BondPayment? = this?.lastOrNull { it.isAmortization && it.hasValue }

fun List<BondPayment>?.lastAmortization(): BondPayment? = this?.lastOrNull { it.isAmortization }

@Serializable
data class BondPayment(
    val type: BondPaymentType,
    val epochDay: Long,
    val value: Double?,
    val valuePercent: Double?,
) {
    operator fun times(multiplier: Double): BondPayment = copy(value = value?.times(multiplier))

    val date: LocalDate
        get() = LocalDate.ofEpochDay(epochDay)
    val valueNotNull: Double
        get() = value ?: 0.0
    val cashFlow: CashFlow?
        get() = value?.let { CashFlow(it, epochDay) }
    val cashFlowNotNull: CashFlow
        get() = CashFlow(valueNotNull, epochDay)
    val hasValue: Boolean
        get() = value != null
    val hasNullValue: Boolean
        get() = value == null
    val isAmortization: Boolean
        get() = type == BondPaymentType.Amortization
    val isCoupon: Boolean
        get() = type == BondPaymentType.Coupon

    companion object {

        fun parseBondPayments(root: JsonObject): Set<BondPayment> =
            buildSet {
                addAll(
                    parsePaymentsBlock(
                        root = root,
                        blockName = "coupons",
                        dateColumn = "coupondate",
                        type = BondPaymentType.Coupon
                    )
                )

                addAll(
                    parsePaymentsBlock(
                        root = root,
                        blockName = "amortizations",
                        dateColumn = "amortdate",
                        type = BondPaymentType.Amortization
                    )
                )
            }

        private fun parsePaymentsBlock(
            root: JsonObject,
            blockName: String,
            dateColumn: String,
            type: BondPaymentType,
        ): List<BondPayment> {

            val block = root[blockName]?.jsonObject
                ?: return emptyList()

            val columns = block["columns"]
                ?.jsonArray
                ?.map { it.jsonPrimitive.contentOrNull }
                ?: return emptyList()

            val dateIndex = columns.indexOf(dateColumn)
            val valueIndex = columns.indexOf("value")
            val valueprcIndex = columns.indexOf("valueprc")

            if (dateIndex == -1 || valueIndex == -1) {
                return emptyList()
            }

            return block["data"]
                ?.jsonArray
                ?.mapNotNull { rowElement ->
                    val row = rowElement.jsonArray

                    val date = row
                        .getOrNull(dateIndex)
                        ?.jsonPrimitive
                        ?.contentOrNull
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            runCatching { LocalDate.parse(it) }.getOrNull()
                        }
                        ?: return@mapNotNull null

                    val value = row
                        .getOrNull(valueIndex)
                        ?.jsonPrimitive
                        ?.contentOrNull
                        ?.toDoubleOrNull()

                    val valueprc = row
                        .getOrNull(valueprcIndex)
                        ?.jsonPrimitive
                        ?.contentOrNull
                        ?.toDoubleOrNull()

                    BondPayment(
                        type = type,
                        epochDay = date.toEpochDay(),
                        value = value,
                        valuePercent = valueprc
                    )
                }
                ?: emptyList()
        }
    }
}