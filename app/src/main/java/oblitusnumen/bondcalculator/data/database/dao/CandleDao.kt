package oblitusnumen.bondcalculator.data.database.dao

import kotlinx.serialization.json.Json
import oblitusnumen.bondcalculator.data.schema.Candle
import oblitusnumen.bondcalculator.data.schema.CandlesSave
import java.io.File

class CandleDao(val cacheDir: File) {
    fun saveCandles(secId: String, fromEpochDay: Long, toEpochDay: Long, candles: List<Candle>) {
        getCandleDir().mkdirs()
        val candleFile = getCandleFile(secId)
        candleFile.writeText(Json.encodeToString(CandlesSave(fromEpochDay, toEpochDay, candles)))
    }

    fun getCandles(secId: String): CandlesSave? {
        getCandleDir().mkdirs()
        val candleFile = getCandleFile(secId)
        return if (!candleFile.exists()) null else Json.decodeFromString(candleFile.readText())
    }

    fun getCandleFile(secId: String) =
        File(getCandleDir(), secId)

    fun getCandleDir(): File =
        File(cacheDir, CANDLE_DIR)

    companion object {
        const val CANDLE_DIR = "candles"
    }
}