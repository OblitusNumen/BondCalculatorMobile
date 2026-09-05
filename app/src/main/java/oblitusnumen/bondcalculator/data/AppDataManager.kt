package oblitusnumen.bondcalculator.data

import AppDatabase
import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import oblitusnumen.bondcalculator.data.database.dao.CandleDao
import oblitusnumen.bondcalculator.data.database.dao.CurrencyRateDao
import oblitusnumen.bondcalculator.data.network.NetworkRequestDispatcher
import oblitusnumen.bondcalculator.data.repository.BondRepository
import oblitusnumen.bondcalculator.data.repository.CandleRepository
import oblitusnumen.bondcalculator.data.repository.CurrencyRateRepository
import oblitusnumen.bondcalculator.data.repository.SecurityRepository

class AppDataManager(context: Context) : AutoCloseable {
    private val database =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bond_calculator.db"
        ).build()

    private val dispatcher = NetworkRequestDispatcher(maxConcurrentRequests = 20)

//    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val bondRepository: BondRepository =
        BondRepository(
            dao = database.bondDetailsDao(),
            dispatcher = dispatcher
        )

    val candleRepository: CandleRepository =
        CandleRepository(
            dao = CandleDao(context.cacheDir),
            dispatcher = dispatcher
        )

    val currencyRateRepository: CurrencyRateRepository =
        CurrencyRateRepository(
            dao = CurrencyRateDao(context.cacheDir),
            dispatcher = dispatcher
        )

    val securityRepository: SecurityRepository =
        SecurityRepository(
            dao = database.securityDao(),
            dispatcher = dispatcher
        )

    override fun close() {
//        scope.cancel()
        database.close()
        dispatcher.close()
    }

    fun setCoroutineScope(coroutineScope: CoroutineScope) {
        bondRepository.setCoroutineScope(coroutineScope)
        securityRepository.setCoroutineScope(coroutineScope)
    }
}