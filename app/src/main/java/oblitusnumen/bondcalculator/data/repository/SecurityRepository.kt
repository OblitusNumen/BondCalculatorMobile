package oblitusnumen.bondcalculator.data.repository

import io.ktor.client.call.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import oblitusnumen.bondcalc.MoexInstrument
import oblitusnumen.bondcalculator.data.database.dao.SecurityDao
import oblitusnumen.bondcalculator.data.database.toDomain
import oblitusnumen.bondcalculator.data.database.toEntity
import oblitusnumen.bondcalculator.data.network.MoexApiClient.Companion.MOEX_ISS
import oblitusnumen.bondcalculator.data.network.NetworkRequestDispatcher
import oblitusnumen.bondcalculator.data.schema.Security
import oblitusnumen.bondcalculator.impl.add
import oblitusnumen.bondcalculator.impl.remove
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

class SecurityRepository(
    private val dao: SecurityDao,
    private val dispatcher: NetworkRequestDispatcher,
    private val cacheTtlSeconds: Long = 10
) {

    private lateinit var coroutineScope: CoroutineScope

    private val securitySubscribers =
        ConcurrentHashMap<MoexInstrument, MutableSet<(Security?, LocalDateTime?) -> Unit>>()

    private fun updateSubs(
        moexInstrument: MoexInstrument,
        security: Security?,
        lastUpdated: LocalDateTime?
    ) {
        securitySubscribers[moexInstrument]?.forEach {
            it(security, lastUpdated)
        }
    }

    // FIXME: no status; pass lastupdtime
    fun getSecuritySubscribe(moexInstrument: MoexInstrument, callback: (Security?, LocalDateTime?) -> Unit) {
        if (securitySubscribers.add(moexInstrument, callback) != 0) {
            println("sub >1 subs ${moexInstrument.name}")
            coroutineScope.launch {
                println(">1 subs ${moexInstrument.name}")
                val cached = dao.get(moexInstrument.security, moexInstrument.board)
                callback(
                    cached?.toDomain(),
                    cached?.getLocalDateTime()
                )
            }
            return
        }
        coroutineScope.launch {
            println("run data get ${moexInstrument.name}")
            var prev = dao.get(moexInstrument.security, moexInstrument.board)
            updateSubs(
                moexInstrument,
                prev?.toDomain(),
                prev?.getLocalDateTime()
            )
            if (prev != null)
                println("cache ${moexInstrument.name}")
            delay(max(prev?.sysTimeEpochSecond?.plus(cacheTtlSeconds)?.minus(Instant.now().epochSecond) ?: 0, 0) * 1000)
            while (securitySubscribers.containsKey(moexInstrument)) {
                println("run request ${moexInstrument.name}")
                dispatcher.get(
                    "$MOEX_ISS/engines/${moexInstrument.engine}/markets/${moexInstrument.market}/boards/${moexInstrument.board}/securities/${moexInstrument.security}.json",
                    {},
                    "security:${moexInstrument.security}",
                    onException = {
                        updateSubs(
                            moexInstrument,
                            prev?.toDomain(),
                            prev?.getLocalDateTime()
                        )
                        println("dispatcher exception:$it ${moexInstrument.name}")
                    }
                ) { response ->
                    println("response success: ${moexInstrument.name}")
                    val body: JsonObject? = response.body()
                    if (body == null) {
                        // FIXME: loading failed/loading
                        updateSubs(
                            moexInstrument,
                            null,
                            LocalDateTime.now(),
                        )
                        return@get
                    }
                    val security = Security.fromJson(body).firstOrNull()
                    prev = security?.toEntity()
                    prev?.let {
                        dao.upsert(it)
                    }
                    updateSubs(
                        moexInstrument,
                        security,
                        security?.getLocalDateTime()
                    )
                }

                delay(cacheTtlSeconds * 1000)
            }
        }
    }

    fun getSecurityUnsubscribe(moexInstrument: MoexInstrument, callback: (Security?, LocalDateTime?) -> Unit) {
        securitySubscribers.remove(moexInstrument, callback)
    }

    fun setCoroutineScope(coroutineScope: CoroutineScope) {
        this.coroutineScope = coroutineScope
    }
}