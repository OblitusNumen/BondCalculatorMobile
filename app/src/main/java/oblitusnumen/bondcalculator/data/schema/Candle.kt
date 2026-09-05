package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import oblitusnumen.bondcalculator.impl.toLocalDateOrNull
import java.time.LocalDate

@Serializable
data class Candle(
    val boardId: String,
    val secId: String,
    val tradeDateEpochDay: Long?,
    val tradeSessionDateEpochDay: Long?,
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
    val tradeDate: LocalDate?
        get() = tradeDateEpochDay?.let { LocalDate.ofEpochDay(it) }
    val tradeSessionDate: LocalDate?
        get() = tradeSessionDateEpochDay?.let { LocalDate.ofEpochDay(it) }

    companion object {
        fun fromMap(map: Map<String, String?>): Candle {
            return Candle(
                boardId = map["BOARDID"].orEmpty(),
                secId = map["SECID"].orEmpty(),

                tradeDateEpochDay = map["TRADEDATE"].toLocalDateOrNull()?.toEpochDay(),
                tradeSessionDateEpochDay = map["TRADE_SESSION_DATE"]?.toLocalDateOrNull()?.toEpochDay(),

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