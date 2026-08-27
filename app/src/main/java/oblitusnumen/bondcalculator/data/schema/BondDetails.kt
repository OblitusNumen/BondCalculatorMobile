package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import oblitusnumen.bondcalculator.data.schema.LocalBond
import java.time.LocalDate

data class BondDetails(
    val bond: Bond,
    val marketData: MarketData,
//    val yield: MarketDataYield,
    val dataVersion: DataVersion
) {
    fun changePrcAccurate() =
        marketData.last?.times(100.0)?.div(bond.prevPrice?:100.0)?.minus(100.0)

    fun toLocalBond(id: Int = 0): LocalBond {
        val matDate = LocalDate.parse(bond.matDate!!)
        return LocalBond(
            id,
            bond.shortname!!,
            bond.faceValue!!,
            matDate.year,
            matDate.monthValue,
            matDate.dayOfMonth,
            bond.couponPeriod!!,
            bond.couponValue!!,
            (marketData.last ?: 100.0) * bond.faceValue / 100,
            LocalBond.getNkdOffset(
                LocalDate.parse(dataVersion.tradeSessionDate),
                bond.accruedInt ?: 0.0,
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
            val versionDataColArray =
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
}