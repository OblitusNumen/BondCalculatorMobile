package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.Serializable

@Serializable
data class CandlesSave(val fromEpochDay: Long, val toEpochDay: Long, val candles: List<Candle>)