package oblitusnumen.bondcalculator.data.repository

import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.serialization.json.JsonObject
import oblitusnumen.bondcalc.MoexInstrument
import oblitusnumen.bondcalculator.data.database.dao.CandleDao
import oblitusnumen.bondcalculator.data.network.MoexApiClient.Companion.MOEX_ISS
import oblitusnumen.bondcalculator.data.network.NetworkRequestDispatcher
import oblitusnumen.bondcalculator.data.schema.Candle
import java.time.LocalDate

class CandleRepository(
    private val dao: CandleDao,
    private val dispatcher: NetworkRequestDispatcher,
) {

    suspend fun getCandles(
        from: LocalDate,
        to: LocalDate,
        instrument: MoexInstrument
    ): List<Candle>? {
        var result: List<Candle>? = null

        dispatcher.get(
            "$MOEX_ISS/history/engines/${instrument.engine}/markets/${instrument.market}/boards/${instrument.board}/securities/${instrument.security}.json",
            {
                parameter("from", from.toString())
                parameter("till", to.toString())
                parameter("limit", 100)
            },
            from to to to instrument,
        ) { response ->
            val body: JsonObject? = response.body()
            result = body?.let { Candle.fromJson(it) } ?: emptyList()
            // TODO: integrate dao
            //dao.upsertAll(result.map { it.toEntity()!! })
        }

        return result
    }
}