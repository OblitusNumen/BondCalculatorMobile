package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import oblitusnumen.bondcalculator.impl.toLocalDateOrNull
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class Security(
    val secId: String,
    val boardId: String,
    val shortName: String?,
    val secName: String?,
    val latName: String?,
    val assetCode: String?,
    val lotVolume: Int?,
    val prevPrice: Double?,
    val bid: Double?,
    val bidDepth: Int?,
    val bidDepthT: Int?,
    val offer: Double?,
    val offerDepth: Int?,
    val offerDepthT: Int?,
    val open: Double?,
    val high: Double?,
    val low: Double?,
    val lastPrice: Double?,
    val qty: Int?,
    val lastChange: Double?,
    val lastChangePrcnt: Double?,
    val numtrades: Int?,
    val volumeToday: Double?,
    val valueToday: Double?,
    val tradeDate: LocalDate?,
    val tradeSessionDate: LocalDate?,
    val lastToPrevPrcnt: Double?,
    val sysTimeEpochSecond: Long?,
) {
    companion object {
        fun fromMap(map: Map<String, String?>): Security {
            return Security(
                secId = map["SECID"].orEmpty(),
                boardId = map["BOARDID"].orEmpty(),
                shortName = map["SHORTNAME"],
                secName = map["SECNAME"],
                latName = map["LATNAME"],
                assetCode = map["ASSETCODE"],
                lotVolume = map["LOTVOLUME"]?.toIntOrNull(),
                prevPrice = map["PREVPRICE"]?.toDoubleOrNull(),
                bid = map["BID"]?.toDoubleOrNull(),
                bidDepth = map["BIDDEPTH"]?.toIntOrNull(),
                bidDepthT = map["BIDDEPTHT"]?.toIntOrNull(),
                offer = map["OFFER"]?.toDoubleOrNull(),
                offerDepth = map["OFFERDEPTH"]?.toIntOrNull(),
                offerDepthT = map["OFFERDEPTHT"]?.toIntOrNull(),
                open = map["OPEN"]?.toDoubleOrNull(),
                high = map["HIGH"]?.toDoubleOrNull(),
                low = map["LOW"]?.toDoubleOrNull(),
                lastPrice = map["LAST"]?.toDoubleOrNull() ?: map["CURRENTVALUE"]?.toDoubleOrNull(),// FIXME:
                qty = map["QUANTITY"]?.toIntOrNull(),
                lastChange = map["LASTCHANGE"]?.toDoubleOrNull(),
                lastChangePrcnt = map["LASTCHANGEPRCNT"]?.toDoubleOrNull(),
                numtrades = map["NUMTRADES"]?.toIntOrNull(),
                volumeToday = map["VOLTODAY"]?.toDoubleOrNull(),
                valueToday = map["VALTODAY"]?.toDoubleOrNull(),
                tradeDate = map["TRADEDATE"]?.toLocalDateOrNull(),
                tradeSessionDate = map["TRADE_SESSION_DATE"]?.toLocalDateOrNull(),
                lastToPrevPrcnt = map["LASTTOPREVPRICE"]?.toDoubleOrNull() ?: map["LASTCHANGEPRC"]?.toDoubleOrNull(),// FIXME:
                sysTimeEpochSecond = map["SYSTIME"]?.let {
                    val dateAndTime = it.split(" ")
                    LocalDate.parse(dateAndTime[0]).atTime(LocalTime.parse(dateAndTime[1]))
                        .atZone(ZoneId.of("Europe/Moscow")).toEpochSecond()
                }
            )
        }

        fun fromJson(root: JsonObject): List<Security> {
            //security
            val securities = root["securities"]?.jsonObject ?: throw IllegalStateException("No 'securities' block")
            val securityColArray = securities["columns"]?.jsonArray ?: throw IllegalStateException("No 'columns' array")
            val securityColList = securityColArray.map { it.jsonPrimitive.content }
            val securityDataArray = securities["data"]?.jsonArray ?: emptyList()

            //market data
            val marketData = root["marketdata"]?.jsonObject ?: throw IllegalStateException("No 'marketdata' block")
            val marketDataColArray =
                marketData["columns"]?.jsonArray ?: throw IllegalStateException("No 'columns' array")
            val marketDataColList = marketDataColArray.map { it.jsonPrimitive.content }
            val marketDataMap: MutableMap<Pair<String, String?>, Map<String, String>> = mutableMapOf()
            (marketData["data"]?.jsonArray ?: emptyList()).forEach { row ->
                val values = row.jsonArray.map { it.jsonPrimitive.content }
                val rowMap = marketDataColList.zip(values).toMap()
                marketDataMap[rowMap["SECID"]!! to rowMap["BOARDID"]!!] = rowMap
            }

            val result: MutableList<Security> = mutableListOf()
            repeat(securityDataArray.size) { index ->
                val securityValues = securityDataArray[index].jsonArray.map { it.jsonPrimitive.content }
                val valuesMap = securityColList.zip(securityValues).toMap()
                result.add(
                    fromMap(
                        valuesMap.toMutableMap()
                            .apply { putAll(marketDataMap[valuesMap["SECID"] to valuesMap["BOARDID"]]!!) })
                )
            }
            return result
        }
    }
}