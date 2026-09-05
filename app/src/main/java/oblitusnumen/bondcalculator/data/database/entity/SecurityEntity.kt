package oblitusnumen.bondcalculator.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Entity(
    tableName = "securities",
    primaryKeys = [
        "secId",
        "boardId"
    ],
    indices = [
        Index(value = ["secId"]),
        Index(value = ["boardId"])
    ]
)
data class SecurityEntity(
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

    val tradeDateEpochDay: Long?,
    val tradeSessionDateEpochDay: Long?,

    val lastToPrevPrcnt: Double?,

    val sysTimeEpochSecond: Long?
) {
    fun getLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime? =
        sysTimeEpochSecond?.let {
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(it),
                zoneId
            )
        }
}