package oblitusnumen.bondcalculator.ui.test

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import oblitusnumen.bondcalculator.data.network.MoexApiClient
import oblitusnumen.bondcalculator.data.schema.Bond
import java.io.Closeable
import java.io.File

class ActivityContainer(
    context: Context
) : Closeable {

    private val databasePath =
        File(
            context.filesDir,
            "bond-calculator.db"
        ).absolutePath

//    val database =
//        AppDatabase(databasePath)
//
//    val bonds =
//        BondRepository(database.manager)
//
//    val marketData =
//        MarketDataRepository(database.manager)

    var bonds: List<Bond> by mutableStateOf(emptyList())

    override fun close() {
//        database.close()
    }

    suspend fun fetch() {
        MoexApiClient().use {
            bonds = fetchAllBonds().map { it.bond }
        }
    }
}