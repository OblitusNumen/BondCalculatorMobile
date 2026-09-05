package oblitusnumen.bondcalculator.data.repository

import BondDetailsEntity
import android.util.Log
import io.ktor.client.call.*
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import oblitusnumen.bondcalculator.data.database.dao.BondDetailsDao
import oblitusnumen.bondcalculator.data.database.toDomain
import oblitusnumen.bondcalculator.data.database.toEntity
import oblitusnumen.bondcalculator.data.network.MoexApiClient.Companion.MOEX_ISS
import oblitusnumen.bondcalculator.data.network.NetworkRequestDispatcher
import oblitusnumen.bondcalculator.data.network.RemoteDataStatus
import oblitusnumen.bondcalculator.data.schema.BondDetails
import oblitusnumen.bondcalculator.data.schema.SecuritySummary
import oblitusnumen.bondcalculator.impl.add
import oblitusnumen.bondcalculator.impl.remove
import oblitusnumen.bondcalculator.impl.urlEncode
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

class BondRepository(
    private val dao: BondDetailsDao,
    private val dispatcher: NetworkRequestDispatcher,
    private val cacheTtlMillis: Long = 30 * 1000
    // FIXME:
) {

    private lateinit var coroutineScope: CoroutineScope

    private val bondSubscribers = ConcurrentHashMap<String, MutableSet<(BondDetails?, LocalDateTime?) -> Unit>>()

//    override fun observeBond(secid: String): Flow<BondDetails?> {
//        return dao.observe(secid)
//            .map { it?.toDomain() }
//    }

    private fun updateSubs(
        secid: String,
        bondDetails: BondDetails?,
        lastUpdated: LocalDateTime?
    ) {
        bondSubscribers[secid]?.forEach {
            it(bondDetails, lastUpdated)
        }
    }

    // FIXME: no status; pass lastupdtime
    fun getBondSubscribe(secid: String, callback: (BondDetails?, LocalDateTime?) -> Unit) {
        if (bondSubscribers.add(secid, callback) != 0) {
            println("sub >1 subs $secid")
            coroutineScope.launch {
                println(">1 subs $secid")
                val cached = dao.get(secid)
                callback(
                    cached?.toDomain(),
                    cached?.getCachedAt()
                )
            }
            return
        }
        coroutineScope.launch {
            println("run data get $secid")
            var prev = dao.get(secid)
            if (prev == null) RemoteDataStatus.Loading else RemoteDataStatus.Cached
            updateSubs(
                secid,
                prev?.toDomain(),
                prev?.getCachedAt()
            )
            if (prev != null)
                println("cache $secid")
            delay(max(prev?.cachedAt?.plus(cacheTtlMillis)?.minus(getTimeMillis()) ?: 0, 0))
            while (bondSubscribers.containsKey(secid)) {
                println("run request $secid")
                dispatcher.get(
                    "$MOEX_ISS/engines/stock/markets/bonds/securities/$secid.json",
                    {},
                    "bond:$secid",
                    onException = {
                        updateSubs(
                            secid,
                            prev?.toDomain(),
                            prev?.getCachedAt()
                        )
                        println("dispatcher exception:$it $secid")
                    }
                ) { response ->
                    println("response success: $secid")
                    val body: JsonObject? = response.body()
                    if (body == null) {
                        // FIXME: loading failed/loading
                        updateSubs(
                            secid,
                            null,
                            LocalDateTime.now()
                        )
                        return@get
                    }
                    val bondDetails = BondDetails.fromJson(body)
                        .filter { it.bond.boardid?.startsWith('T', true) ?: false }
                    val bond: BondDetails?
                    if (bondDetails.size <= 1) {
                        bond = bondDetails.firstOrNull()
                        // FIXME: mb stop coroutine
                    } else {
                        Log.w("MOEX API", "Fetched multiple bonds!!!")
                        bondDetails.forEach {
                            Log.w("MOEX API", "\t${it.bond.secid}")
                        }

                        val coherent = bondDetails.firstOrNull { it.bond.faceUnit == it.bond.currencyId }
                        val sur = bondDetails.firstOrNull { it.bond.currencyId.equals("SUR", true) }

                        bond =
                            if (coherent == null) bondDetails.firstOrNull() else if (sur == null) coherent else BondDetails(
                                coherent.bond,
                                sur.marketData,
                                coherent.dataVersion
                            )
                    }
                    prev = bond?.toEntity(getTimeMillis())
                    prev?.let {
                        dao.upsert(it)
                    }
                    updateSubs(
                        secid,
                        bond,
                        prev?.getCachedAt()
                    )
                }

                delay(cacheTtlMillis)
            }
        }
    }

    fun getBondUnsubscribe(secid: String, callback: (BondDetails?, LocalDateTime?) -> Unit) {
        bondSubscribers.remove(secid, callback)
    }

    // FIXME:
    fun refreshBond(secid: String) {
        coroutineScope.launch {
            var prev: BondDetailsEntity? = null
            if (prev == null) RemoteDataStatus.Loading else RemoteDataStatus.Cached
            println("run request $secid")
            dispatcher.get(
                "$MOEX_ISS/engines/stock/markets/bonds/securities/$secid.json",
                {},
                "bond:$secid",
                onException = {
                    //                status = if (prev == null) RemoteDataStatus.Loading else RemoteDataStatus.Cached
                    //                bondSubUpdater(
                    //                    secid,
                    //                    prev?.toDomain(),
                    //                    status
                    //                )
                    //                println("dispatcher exception:$it $secid")
                }
            ) { response ->
                println("response success: $secid")
                val body: JsonObject? = response.body()
                if (body == null) {
                    // FIXME: loading failed/loading
                    updateSubs(
                        secid,
                        null,
                        LocalDateTime.now()
                    )
                    return@get
                }
                val bondDetails = BondDetails.fromJson(body)
                    .filter { it.bond.boardid?.startsWith('T', true) ?: false }
                val bond: BondDetails?
                if (bondDetails.size <= 1) {
                    bond = bondDetails.firstOrNull()
                    // FIXME: mb stop coroutine
                } else {
                    Log.w("MOEX API", "Fetched multiple bonds!!!")
                    bondDetails.forEach {
                        Log.w("MOEX API", "\t${it.bond.secid}")
                    }

                    val coherent = bondDetails.firstOrNull { it.bond.faceUnit == it.bond.currencyId }
                    val sur = bondDetails.firstOrNull { it.bond.currencyId.equals("SUR", true) }

                    bond =
                        if (coherent == null) bondDetails.firstOrNull() else if (sur == null) coherent else BondDetails(
                            coherent.bond,
                            sur.marketData,
                            coherent.dataVersion
                        )
                }
                prev = bond?.toEntity(getTimeMillis())
                prev?.let {
                    dao.upsert(it)
                }
                updateSubs(
                    secid,
                    bond,
                    prev?.getCachedAt()
                )
            }
        }
    }

    suspend fun searchBonds(query: String, callback: (RemoteDataStatus, List<Pair<String, String?>>?) -> Unit) {
        var status = RemoteDataStatus.Loading
        println("run query request: $query")
        dispatcher.get(
            "$MOEX_ISS/securities.json?q=${urlEncode(query)}",
            {},
            "query:$query",
            false,
            onException = {
                println("query response failed: $query")
                status = RemoteDataStatus.Loading
                callback(status, null)
            }
        ) { response ->
            println("query response success: $query")
            val body: JsonObject? = response.body()
            status = RemoteDataStatus.Loaded
            val securitiesObj = body?.get("securities")?.jsonObject
            val bondDetails: List<Pair<String, String?>> =
                if (securitiesObj == null) {
                    emptyList()
                } else {
                    val columns = securitiesObj["columns"]?.jsonArray
                        ?.map { it.jsonPrimitive.content }
                    if (columns == null) {
                        emptyList()
                    } else {
                        val dataArray = securitiesObj["data"]?.jsonArray
                        if (dataArray == null) {
                            emptyList()
                        } else {
                            dataArray.map { element ->
                                val values = element.jsonArray.map { it.jsonPrimitive.content }
                                val map = columns.zip(values).toMap()
                                SecuritySummary(
                                    secid = map["secid"] ?: "",
                                    group = map["group"],
                                    type = map["type"],
                                    isin = map["isin"],
                                    shortname = map["shortname"],
                                    name = map["name"]
                                )
                            }.filter { it.group == "stock_bonds" }.filter {
                                it.shortname?.contains(query, true) ?: false ||
                                        it.name?.contains(query, true) ?: false ||
                                        it.isin?.contains(query, true) ?: false
                            }
                                .map { it.secid to it.shortname }
                        }
                    }
                }
            callback(status, bondDetails)
        }

        if (status == RemoteDataStatus.Loading) {
            status = RemoteDataStatus.Cached
            val cached = AtomicReference<List<Pair<String, String?>>?>(null)
            Thread {
                cached.set(dao.search(query).map { it.secid to it.shortName })
            }.start()
            while (cached.get() == null)
                delay(100)
            callback(status, cached.get())
        }
    }

    fun setCoroutineScope(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope
    }
}