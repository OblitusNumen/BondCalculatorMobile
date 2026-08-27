package oblitusnumen.bondcalculator.data.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "candles",
    primaryKeys = [
        "boardId",
        "secId",
        "tradeDate"
    ],
    indices = [
        Index(value = ["secId", "tradeDate"]),
        Index(value = ["boardId", "secId", "tradeDate"])
    ]
)
data class CandleEntity(
    val boardId: String,
    val secId: String,

    val tradeDateEpochDay: Long,

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
)