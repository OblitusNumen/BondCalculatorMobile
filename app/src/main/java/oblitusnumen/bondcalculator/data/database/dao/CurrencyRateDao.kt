package oblitusnumen.bondcalculator.data.database.dao

import kotlinx.serialization.json.Json
import oblitusnumen.bondcalculator.data.schema.CurrencyRate
import java.io.File
import java.time.LocalDate

class CurrencyRateDao(val cacheDir: File) {
    val json = Json

    val ratesFile
        get() = File(cacheDir, "rates.json")
    val ratesMetaFile
        get() = File(cacheDir, "rates_meta.json")

    fun getCachedDate(): LocalDate? =
        ratesMetaFile.takeIf { it.exists() }?.readText()?.let { LocalDate.parse(it) }

    fun getRates(): Map<String, CurrencyRate>? =
        ratesFile.takeIf { it.exists() }?.readText()?.let { json.decodeFromString(it) }

    fun putCurrencies(currencyRates: Map<String, CurrencyRate>, cachedAt: LocalDate) {
        ratesFile.writeText(json.encodeToString(currencyRates))
        ratesMetaFile.writeText(cachedAt.toString())
    }
}

//import androidx.room.Dao
//import androidx.room.Query
//import androidx.room.Upsert
//import kotlinx.coroutines.flow.Flow
//import oblitusnumen.bondcalculator.data.database.entity.CurrencyEntity
//import java.time.LocalDate
//
//@Dao
//interface CurrencyDao {
//
//    @Query("""
//        SELECT cacheDate
//        FROM currencies
//        LIMIT 1
//    """)
//    fun getCacheDate(): LocalDate?
//
//    @Query("""
//        SELECT *
//        FROM currencies
//        WHERE id = :id
//    """)
//    suspend fun get(id: String): CurrencyEntity?
//
//    @Query("""
//        SELECT *
//        FROM currencies
//    """)
//    fun getAll(): List<CurrencyEntity>
//
//    @Query("""
//        SELECT *
//        FROM currencies
//        WHERE id = :id
//    """)
//    fun observe(id: String): Flow<CurrencyEntity?>
//
//    @Query("""
//        SELECT *
//        FROM currencies
//        ORDER BY charCode
//    """)
//    fun observeAll(): Flow<List<CurrencyEntity>>
//
//    @Upsert
//    suspend fun upsert(currency: CurrencyEntity)
//
//    @Upsert
//    suspend fun upsertAll(currencies: List<CurrencyEntity>)
//
//    @Query("DELETE FROM currencies")
//    suspend fun deleteAll()
//}