package oblitusnumen.bondcalculator.data.network

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.*
import oblitusnumen.bondcalc.MoexInstrument
import oblitusnumen.bondcalculator.data.schema.BondDetails
import oblitusnumen.bondcalculator.data.schema.Candle
import oblitusnumen.bondcalculator.data.schema.SecuritySummary
import oblitusnumen.bondcalculator.impl.urlEncode
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

class MoexApiClient : AutoCloseable {
    //    private val client = HttpClient(CIO)
    val client = HttpClient(CIO) {
        install(ContentNegotiation.Plugin) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
    }
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCandles(
        instrument: MoexInstrument,
        from: LocalDate,
        till: LocalDate
    ): List<Candle> {
        val response: JsonObject = client.get(
            "$MOEX_ISS/history/engines/${instrument.engine}/markets/${instrument.market}/boards/${instrument.board}/securities/${instrument.security}.json"
        ) {
            parameter("from", from.toString())
            parameter("till", till.toString())
            parameter("limit", 1000)
        }.body()

        val history = response["history"]
            ?.jsonObject
            ?: error("В ответе отсутствует history")

        val columns = history["columns"]
            ?.jsonArray
            ?: error("В history отсутствует columns")

        val data = history["data"]
            ?.jsonArray
            ?: return emptyList()

        val columnNames = columns.map {
            it.jsonPrimitive.content
        }

        fun index(name: String): Int =
            columnNames.indexOfFirst {
                it.equals(name, ignoreCase = true)
            }

        val dateIndex = index("TRADEDATE")
        val openIndex = index("OPEN")
        val highIndex = index("HIGH")
        val lowIndex = index("LOW")
        val closeIndex = index("CLOSE")
        val volumeIndex = index("VALUE")

        fun JsonArray.doubleAt(index: Int): Double? =
            if (index >= 0 && index < size) {
                this[index].jsonPrimitive.doubleOrNull
            } else {
                null
            }

        return data.mapNotNull { element ->

            val row = element.jsonArray

            val date = row
                .getOrNull(dateIndex)
                ?.jsonPrimitive
                ?.contentOrNull
                ?.let {
                    runCatching {
                        LocalDate.parse(it)
                    }.getOrNull()
                }
                ?: return@mapNotNull null

            Candle(
                "null", "null",
                tradeDateEpochDay = date.toEpochDay(),
                null, null, null,
                open = row.doubleAt(openIndex),
                close = row.doubleAt(closeIndex),
                high = row.doubleAt(highIndex),
                low = row.doubleAt(lowIndex),
                volume = row.doubleAt(volumeIndex),
                null, null, null, null
            )
        }
    }

    suspend fun getInstrumentPrice(instrument: MoexInstrument): Pair<Double?, Double?> {
        val response: JsonObject = client.get(
            "$MOEX_ISS/engines/${instrument.engine}/markets/${instrument.market}/securities/${instrument.security}.json".apply {
                println(
                    this
                )
            }
        ).body()

        val marketdata = response["marketdata"]
            ?.jsonObject
            ?: error("В ответе отсутствует marketdata")

        val columns = marketdata["columns"]
            ?.jsonArray
            ?: error("В marketdata отсутствует columns")

        val data = marketdata["data"]
            ?.jsonArray
            ?: return null to null

        val columnNames = columns.map {
            it.jsonPrimitive.content
        }

        fun index(name: String): Int =
            columnNames.indexOfFirst {
                it.equals(name, ignoreCase = true)
            }

        val lastPriceIndex = index("LAST").let { index -> if (index < 0) index("CURRENTVALUE") else index }
        val changePrcIndex = index("LASTTOPREVPRICE").let { index -> if (index < 0) index("LASTCHANGEPRC") else index }

        fun JsonArray.doubleAt(index: Int): Double? =
            if (index >= 0 && index < size) {
                this[index].jsonPrimitive.doubleOrNull
            } else {
                null
            }

        return data.firstNotNullOfOrNull { element ->
            val row = element.jsonArray
            row.doubleAt(lastPriceIndex) to row.doubleAt(changePrcIndex)
        } ?: (0.0 to 0.0)
    }

    /**
     * Search for bonds by query string, filter for stock_bonds group,
     * and fetch full details for each matching bond.
     *
     * @param query Search term (matches ISIN, name, secid, etc.)
     * @return List of BondDetails for all matching bonds in the stock_bonds group
     */
    suspend fun searchAndFetchBonds(query: String): List<BondDetails> {
        // 1. Search for securities matching the query
        val searchResults = searchSecurities(query)

        // 2. Filter to only stock_bonds (bonds traded on the stock market)
        val bondSecIds = searchResults
            .filter { it.group == "stock_bonds" }
            .map { it.secid }

        if (bondSecIds.isEmpty()) {
            return emptyList()
        }

        // 3. Fetch full details for each bond
        val bonds: Array<BondDetails?> = coroutineScope {
            bondSecIds.mapIndexed { index, secid ->
                async {
                    try {
                        fetchBond(secid)
                    } catch (e: Exception) {
                        println("Failed to fetch details for $secid: ${e.message}")
                        if (e is CancellationException) throw e
                        null
                    }
                }
            }.awaitAll().toTypedArray()
        }

        return bonds.toList().filter { it != null } as List<BondDetails>
    }

    /**
     * Search securities by query string.
     * Returns a list of basic security info (secid, group, etc.)
     */
    suspend fun searchBonds(query: String): List<BondDetails> {
        val url = "$MOEX_ISS/engines/stock/markets/bonds/securities.json?q=${urlEncode(query)}"
        val responseText: String = client.get(url).bodyAsText()
        val root = json.parseToJsonElement(responseText).jsonObject

        return BondDetails.fromJson(root)
    }

    /**
     * Search securities by query string.
     * Returns a list of basic security info (secid, group, etc.)
     */
    suspend fun searchSecurities(query: String): List<SecuritySummary> {
        val url = "$MOEX_ISS/securities.json?q=${urlEncode(query)}"
        val responseText: String = client.get(url).bodyAsText()
        val root = json.parseToJsonElement(responseText).jsonObject

        val securitiesObj = root["securities"]?.jsonObject
            ?: return emptyList()

        val columns = securitiesObj["columns"]?.jsonArray
            ?.map { it.jsonPrimitive.content }
            ?: return emptyList()

        val dataArray = securitiesObj["data"]?.jsonArray ?: return emptyList()

        return dataArray.map { element ->
            val values = element.jsonArray.map { it.jsonPrimitive.content }
            val map = columns.zip(values).toMap()
            SecuritySummary(
                secid = map["secid"] ?: "",
                group = map["group"],
                type = map["type"],
                isin = map["isin"],
                shortname = map["shortname"],
                name = map["name"]
            )
        }
    }

    /**
     * Fetch full bond details for a specific secid.
     * The response contains a 'description' block where each row
     * represents a single field (name, title, value, type, ...).
     */
    suspend fun fetchBond(secid: String): BondDetails? {
        val url = "$MOEX_ISS/engines/stock/markets/bonds/securities/$secid.json"//?primary_board=1
        val responseText: String = client.get(url).bodyAsText()
//        return BondDetails.fromJson(json.parseToJsonElement(responseText).jsonObject)
//            .filter { it.bond.boardid?.startsWith('T', true) ?: false && it.bond.faceUnit == it.bond.currencyId }
//            .apply {
//                if (size > 1) {
//                    Log.e("MOEX API", "Fetched multiple bonds!!!")
//                    forEach {
//                        Log.e("MOEX API", "\t${it.bond.secid}")
//                    }
//                }
//            }.firstOrNull()
        val bondDetails = BondDetails.fromJson(json.parseToJsonElement(responseText).jsonObject)
            .filter { it.bond.boardid?.startsWith('T', true) ?: false }
        if (bondDetails.size <= 1)
            return bondDetails.firstOrNull()
        Log.w("MOEX API", "Fetched multiple bonds!!!")
        bondDetails.forEach {
            Log.w("MOEX API", "\t${it.bond.secid}")
        }

        val coherent = bondDetails.firstOrNull { it.bond.faceUnit == it.bond.currencyId }
        val sur = bondDetails.firstOrNull { it.bond.currencyId.equals("SUR", true) }

        return if (coherent == null) bondDetails.firstOrNull() else if (sur == null) coherent else BondDetails(
            coherent.bond,
            sur.marketData,
//            sur.yield,
            coherent.dataVersion
        )
    }

    suspend fun fetchAllBonds(): List<BondDetails> {
        val url = "$MOEX_ISS/engines/stock/markets/bonds/securities.json?primary_board=1"
        val responseText: String = client.get(url).bodyAsText()
        return BondDetails.fromJson(json.parseToJsonElement(responseText).jsonObject)
    }

    override fun close() = client.close()

    suspend fun use(onException: ((Exception) -> Unit)? = null, block: suspend MoexApiClient.() -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
            onException?.invoke(e)
            if (e is CancellationException) throw e
        } finally {
            close()
        }
    }

    companion object {
        const val MOEX_ISS = "https://iss.moex.com/iss"
    }
}