package oblitusnumen.bondcalculator.impl.moexapi.schema

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate

data class BondDetails(val bond: Bond, val marketData: MarketData, val yield: MarketDataYield) {
    fun toLocalBond(id: Int): oblitusnumen.bondcalculator.impl.Bond {
        val matDate = LocalDate.parse(bond.matDate!!)
        return oblitusnumen.bondcalculator.impl.Bond(
            id,
            bond.shortname!!,
            bond.faceValue!!,
            matDate.year,
            matDate.monthValue,
            matDate.dayOfMonth,
            bond.couponPeriod!!,
            bond.couponValue!!,
            marketData.last!! * bond.faceValue / 100,
            // FIXME: accrued date LocalDate.parse(bond.settleDate)
            oblitusnumen.bondcalculator.impl.Bond.getNkdOffset(
                LocalDate.now(),
                bond.accruedInt!!,
                bond.couponValue,
                matDate,
                bond.couponPeriod
            )
        )
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
            val marketDataMap: MutableMap<String, MarketData> = mutableMapOf()
            (marketData["data"]?.jsonArray ?: emptyList()).forEach { row ->
                MarketData.fromRow(row.jsonArray.map { it.jsonPrimitive.content }).apply { marketDataMap[secid] = this }
            }

            //yields data
            val yieldsData =
                root["marketdata_yields"]?.jsonObject ?: throw IllegalStateException("No 'marketdata_yields' block")
            val yieldsDataColArray =
                yieldsData["columns"]?.jsonArray ?: throw IllegalStateException("No 'columns' array")
            yieldsDataColArray.map { it.jsonPrimitive.content }
            val yieldsDataMap: MutableMap<String, MarketDataYield> = mutableMapOf()
            (yieldsData["data"]?.jsonArray ?: emptyList()).forEach { row ->
                MarketDataYield.fromRow(row.jsonArray.map { it.jsonPrimitive.content })
                    .apply { yieldsDataMap[secid] = this }
            }

            val result: MutableList<BondDetails> = mutableListOf()

            repeat(bondDataArray.size) { index ->
                val bondValues = bondDataArray[index].jsonArray.map { it.jsonPrimitive.content }
                Bond.fromMap(bondColList.zip(bondValues).toMap()).apply {
                    result.add(
                        BondDetails(
                            this,
                            marketDataMap[secid] ?: MarketData(secid),
                            yieldsDataMap[secid] ?: MarketDataYield(secid),
                        )
                    )
                }
            }

            return result
        }
    }
}