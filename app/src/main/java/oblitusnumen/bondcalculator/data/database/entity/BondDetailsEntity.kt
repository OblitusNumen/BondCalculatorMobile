import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bond_details")
data class BondDetailsEntity(
    @PrimaryKey
    val secid: String,

    // Bond
    val boardId: String?,
    val shortName: String?,
    val couponValue: Double?,
    val accruedInt: Double?,
    val prevPrice: Double?,
    val lotSize: Int?,
    val faceValue: Double?,
    val boardName: String?,
    val matDate: String?,
    val decimals: Int?,
    val couponPeriod: Int?,
    val issueSize: Long?,
    val secName: String?,
    val faceUnit: String?,
    val isin: String?,
    val latName: String?,
    val regNumber: String?,
    val currencyId: String?,
    val issueSizePlaced: Long?,
    val couponPercent: Double?,
    val lotValue: Double?,
    val callOptionDate: String?,
    val putOptionDate: String?,
    val bondType: String?,
    val bondSubType: String?,

    // MarketData
    val bid: Double?,
    val offer: Double?,
    val spread: Double?,
    val bidDepthT: Long?,
    val offerDepthT: Long?,
    val open: Double?,
    val low: Double?,
    val high: Double?,
    val last: Double?,
    val lastChange: Double?,
    val lastChangePrcnt: Double?,
    val value: Double?,
    val yield: Double?,
    val valueUsd: Double?,
    val marketPriceToday: Double?,
    val marketPrice: Double?,
    val lastToPrevPrice: Double?,
    val numTrades: Int?,
    val volToday: Long?,
    val valToday: Long?,
    val valTodayUsd: Long?,
    val marketBoardId: String?,
    val duration: Double?,
    val change: Double?,
    val seqNum: Long?,
    val valTodayRur: Long?,
    val yieldToOffer: Double?,
    val callOptionYield: Double?,
    val callOptionDuration: Double?,

    // DataVersion
    val dataVersion: Int,
    val dataSeqNum: Long,
    val tradeDate: String,
    val tradeSessionDate: String,

    // Local cache metadata
    val cachedAt: Long
) {
    fun isFresh(currentTimeMillis: Long, cacheTtlMillis: Long) = cachedAt + cacheTtlMillis > currentTimeMillis
}