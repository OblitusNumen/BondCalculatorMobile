package oblitusnumen.bondcalculator.impl.moexapi.schema

import kotlinx.serialization.Serializable

@Serializable
data class MarketDataYield(
    val secid: String,
    val boardid: String? = null,
    val price: Double? = null,
    val yieldDate: String? = null,
    val zcycMoment: String? = null,
    val yieldDateType: String? = null,
    val effectiveYield: Double? = null,
    val duration: Int? = null,
    val zSpreadBp: Int? = null,
    val gSpreadBp: Int? = null,
    val waPrice: Double? = null,
    val effectiveYieldWaPrice: Double? = null,
    val durationWaPrice: Int? = null,
    val ir: Double? = null,
    val icpi: Double? = null,
    val bei: Double? = null,
    val cbr: Double? = null,
    val yieldToOffer: Double? = null,
    val yieldLastCoupon: Double? = null,
    val tradeMoment: String? = null,
    val seqNum: Long? = null,
    val sysTime: String? = null
) {
    companion object {
        fun fromRow(row: List<String?>): MarketDataYield = MarketDataYield(
            secid = row.getOrNull(0) ?: "",
            boardid = row.getOrNull(1),
            price = row.getOrNull(2)?.toDoubleOrNull(),
            yieldDate = row.getOrNull(3),
            zcycMoment = row.getOrNull(4),
            yieldDateType = row.getOrNull(5),
            effectiveYield = row.getOrNull(6)?.toDoubleOrNull(),
            duration = row.getOrNull(7)?.toIntOrNull(),
            zSpreadBp = row.getOrNull(8)?.toIntOrNull(),
            gSpreadBp = row.getOrNull(9)?.toIntOrNull(),
            waPrice = row.getOrNull(10)?.toDoubleOrNull(),
            effectiveYieldWaPrice = row.getOrNull(11)?.toDoubleOrNull(),
            durationWaPrice = row.getOrNull(12)?.toIntOrNull(),
            ir = row.getOrNull(13)?.toDoubleOrNull(),
            icpi = row.getOrNull(14)?.toDoubleOrNull(),
            bei = row.getOrNull(15)?.toDoubleOrNull(),
            cbr = row.getOrNull(16)?.toDoubleOrNull(),
            yieldToOffer = row.getOrNull(17)?.toDoubleOrNull(),
            yieldLastCoupon = row.getOrNull(18)?.toDoubleOrNull(),
            tradeMoment = row.getOrNull(19) as? String,
            seqNum = row.getOrNull(20)?.toLongOrNull(),
            sysTime = row.getOrNull(21) as? String
        )
    }
}