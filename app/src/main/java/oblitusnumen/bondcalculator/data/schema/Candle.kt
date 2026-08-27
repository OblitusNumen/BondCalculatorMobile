package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import oblitusnumen.bondcalculator.impl.toLocalDateOrNull
import java.time.LocalDate

data class Candle(
    val boardId: String,
    val secId: String,
    val tradeDate: LocalDate?,
    val tradeSessionDate: LocalDate?,
    val shortname: String?,
    val name: String?,
    val open: Double?,
    val close: Double?,
    val high: Double?,
    val low: Double?,
    val volume: Double?,
    val value: Double?,
    val numTrades: Int?,
    val marketCap: Double?,
    val currencyId: String?,
) {
    companion object {
        fun fromMap(map: Map<String, String?>): Candle {
            return Candle(
                boardId = map["BOARDID"].orEmpty(),
                secId = map["SECID"].orEmpty(),

                tradeDate = map["TRADEDATE"].toLocalDateOrNull(),
                tradeSessionDate = map["TRADE_SESSION_DATE"]?.toLocalDateOrNull(),

                shortname = map["SHORTNAME"],
                name = map["NAME"],

                open = map["OPEN"]?.toDoubleOrNull(),
                close = map["CLOSE"]?.toDoubleOrNull(),
                high = map["HIGH"]?.toDoubleOrNull(),
                low = map["LOW"]?.toDoubleOrNull(),

                volume = map["VOLUME"]?.toDoubleOrNull(),
                value = map["VALUE"]?.toDoubleOrNull(),
                numTrades = map["NUMTRADES"]?.toIntOrNull(),
                marketCap = map["CAPITALIZATION"]?.toDoubleOrNull(),

                currencyId = map["CURRENCYID"]
            )
        }

        fun fromJson(root: JsonObject): List<Candle> {
            //bond
            val history = root["history"]?.jsonObject ?: throw IllegalStateException("No 'history' block")
            val colArray = history["columns"]?.jsonArray ?: throw IllegalStateException("No 'columns' array")
            val colList = colArray.map { it.jsonPrimitive.content }
            val dataArray = history["data"]?.jsonArray ?: emptyList()

            val result: MutableList<Candle> = mutableListOf()
            repeat(dataArray.size) { index ->
                val values = dataArray[index].jsonArray.map { it.jsonPrimitive.content }
                result.add(fromMap(colList.zip(values).toMap()))
            }
            return result
        }
    }
}