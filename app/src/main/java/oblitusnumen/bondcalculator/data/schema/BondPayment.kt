package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate

fun List<BondPayment>?.lastCoupon(): BondPayment? = this?.lastOrNull { it.type == BondPaymentType.Coupon }

fun List<BondPayment>?.firstCoupon(): BondPayment? = this?.firstOrNull { it.type == BondPaymentType.Coupon }

fun List<BondPayment>?.lastAmortization(): BondPayment? = this?.lastOrNull { it.type == BondPaymentType.Amortization }

@Serializable
data class BondPayment(
    val type: BondPaymentType,
    val epochDay: Long,
    val value: Double,
    val valuePercent: Double,
) {
    operator fun times(ratio: Double): BondPayment = copy(value = value * ratio)

    val date: LocalDate
        get() = LocalDate.ofEpochDay(epochDay)

    val cashFlow: CashFlow
        get() = CashFlow(value, epochDay)

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
                        ?: return@mapNotNull null

                    val valueprc = row
                        .getOrNull(valueprcIndex)
                        ?.jsonPrimitive
                        ?.contentOrNull
                        ?.toDoubleOrNull()
                        ?: return@mapNotNull null

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