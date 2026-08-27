package oblitusnumen.bondcalculator.data.database.dao

class CandleDao
//import androidx.room.Dao
//import androidx.room.Query
//import androidx.room.Upsert
//import kotlinx.coroutines.flow.Flow
//import oblitusnumen.bondcalculator.data.database.entity.CandleEntity
//import java.time.LocalDate
//
//@Dao
//interface CandleDao {
//
//    @Query("""
//        SELECT *
//        FROM candles
//        WHERE secId = :secId
//        ORDER BY tradeDate ASC
//    """)
//    fun observeAll(
//        secId: String
//    ): Flow<List<CandleEntity>>
//
//    @Query("""
//        SELECT *
//        FROM candles
//        WHERE secId = :secId
//          AND tradeDate BETWEEN :from AND :to
//        ORDER BY tradeDate ASC
//    """)
//    fun observeRange(
//        secId: String,
//        from: LocalDate,
//        to: LocalDate
//    ): Flow<List<CandleEntity>>
//
//    @Query("""
//        SELECT *
//        FROM candles
//        WHERE secId = :secId
//          AND tradeDate BETWEEN :from AND :to
//        ORDER BY tradeDate ASC
//    """)
//    suspend fun getRange(
//        secId: String,
//        from: LocalDate,
//        to: LocalDate
//    ): List<CandleEntity>
//
//    @Query("""
//        SELECT *
//        FROM candles
//        WHERE secId = :secId
//          AND tradeDate = :date
//        LIMIT 1
//    """)
//    suspend fun get(
//        secId: String,
//        date: LocalDate
//    ): CandleEntity?
//
//    @Upsert
//    suspend fun upsert(candle: CandleEntity)
//
//    @Upsert
//    suspend fun upsertAll(candles: List<CandleEntity>)
//
//    @Query("""
//        DELETE FROM candles
//        WHERE secId = :secId
//    """)
//    suspend fun deleteAll(secId: String)
//}