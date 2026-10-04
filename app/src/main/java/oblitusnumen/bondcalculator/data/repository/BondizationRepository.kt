package oblitusnumen.bondcalculator.data.repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import oblitusnumen.bondcalculator.data.database.dao.BondizationDao
import oblitusnumen.bondcalculator.data.network.MoexApiClient.Companion.MOEX_ISS
import oblitusnumen.bondcalculator.data.network.NetworkRequestDispatcher
import oblitusnumen.bondcalculator.data.schema.BondPayment

class BondizationRepository(
    private val dao: BondizationDao,
    private val dispatcher: NetworkRequestDispatcher,
) {

    suspend fun getBondization(
        bondSecId: String
    ): List<BondPayment>? {
        val bondizationCache = dao.getBondization(bondSecId)
        if (bondizationCache != null)
            return bondizationCache

        val urlString = "$MOEX_ISS/statistics/engines/stock/markets/bonds/bondization/${bondSecId}.json"
        val limit = 100
        val result = mutableSetOf<BondPayment>()

        val failed = mutableSetOf<Int>()
        var start = 0

        println(urlString)

        while (true) {
            var quit = true
            failed += start

            dispatcher.get(
                urlString,
                {
                    parameter("start", start)
                    parameter("limit", limit)
                    parameter("iss.only", "coupons,amortizations")
                },
                bondSecId to start,
                onException = {
                println("Response bondization failure: $it")
                }
            ) { response ->
                println("Response bondization")

                val body: JsonObject = response.body() ?: throw IllegalStateException("Bond payments parsing failed")
                result.addAll(BondPayment.parseBondPayments(body))

                val amortizationsCursor = body["amortizations.cursor"]?.jsonObject
                val couponsCursor = body["coupons.cursor"]?.jsonObject

                val amortizationsCursorColumns = amortizationsCursor?.get("columns")
                    ?.jsonArray
                    ?.map { it.jsonPrimitive.content }
                val couponsCursorColumns = couponsCursor?.get("columns")
                    ?.jsonArray
                    ?.map { it.jsonPrimitive.content }

                val totalAmortizationsIndex = amortizationsCursorColumns?.indexOf("TOTAL")
                val totalCouponsIndex = couponsCursorColumns?.indexOf("TOTAL")

                val totalAmortizations =
                    if (totalAmortizationsIndex == null || totalAmortizationsIndex < 0)
                        0
                    else
                        amortizationsCursor["data"]?.jsonArray?.firstOrNull()?.jsonArray?.getOrNull(
                            totalAmortizationsIndex
                        )?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                val totalCoupons =
                    if (totalCouponsIndex == null || totalCouponsIndex < 0)
                        0
                    else
                        couponsCursor["data"]?.jsonArray?.firstOrNull()?.jsonArray?.getOrNull(totalCouponsIndex)?.jsonPrimitive?.content?.toIntOrNull()
                            ?: 0

                quit = totalAmortizations < start + limit && totalCoupons < start + limit
                failed.remove(start)
            }

            if (quit || failed.isNotEmpty())
                break
            start += limit
        }

        // FIXME:
        if (failed.isNotEmpty())
            return null
//        while (failed.isNotEmpty()) {
//            failed.toList().forEach { start ->
//                dispatcher.get(
//                    urlString,
//                    {
//                        parameter("start", start)
//                        parameter("limit", limit)
//                        parameter("iss.only", "coupons,amortizations")
//                    },
//                    bondSecId to start,
//                ) { response ->
//                    val body: JsonObject? = response.body()
//
//
//                    failed -= start
//                }
//            }
//        }

        val bondization: List<BondPayment> = result.sortedWith(compareBy<BondPayment> { it.epochDay }.thenBy { it.type })
        dao.saveBondization(bondSecId, bondization)

        return bondization
    }
}