package oblitusnumen.bondcalculator.impl.moexapi

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import oblitusnumen.bondcalculator.impl.moexapi.schema.BondDetails
import oblitusnumen.bondcalculator.impl.moexapi.schema.SecuritySummary
import oblitusnumen.bondcalculator.impl.urlEncode

class MoexApiClient : AutoCloseable {
    private val client = HttpClient(CIO)
    private val json = Json { ignoreUnknownKeys = true }

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
    suspend fun searchSecurities(query: String): List<SecuritySummary> {
        val url = "https://iss.moex.com/iss/securities.json?q=${urlEncode(query)}&primary_board=1"
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
        val url = "https://iss.moex.com/iss/engines/stock/markets/bonds/securities/$secid.json?primary_board=1"
        val responseText: String = client.get(url).bodyAsText()
        return BondDetails.fromJson(json.parseToJsonElement(responseText).jsonObject).firstOrNull()
    }

    suspend fun fetchAllBonds(): List<BondDetails> {
        val url = "https://iss.moex.com/iss/engines/stock/markets/bonds/securities.json?primary_board=1"
        val responseText: String = client.get(url).bodyAsText()
        return BondDetails.fromJson(json.parseToJsonElement(responseText).jsonObject)
    }

    override fun close() = client.close()
}