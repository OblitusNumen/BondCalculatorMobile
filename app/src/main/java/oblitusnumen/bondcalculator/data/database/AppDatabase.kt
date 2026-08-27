import androidx.room.Database
import androidx.room.RoomDatabase
import oblitusnumen.bondcalculator.data.database.dao.BondDetailsDao
import oblitusnumen.bondcalculator.data.database.dao.SecurityDao
import oblitusnumen.bondcalculator.data.database.entity.SecurityEntity

@Database(
    entities = [
        BondDetailsEntity::class,
//        CandleEntity::class,
//        CurrencyEntity::class,
        SecurityEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bondDetailsDao(): BondDetailsDao

//    abstract fun candleDao(): CandleDao

//    abstract fun currencyDao(): CurrencyDao

    abstract fun securityDao(): SecurityDao
}