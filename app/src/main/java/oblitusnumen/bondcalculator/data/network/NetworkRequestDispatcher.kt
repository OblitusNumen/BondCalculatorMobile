package oblitusnumen.bondcalculator.data.network

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import oblitusnumen.bondcalculator.data.network.NetworkRequestDispatcher.Companion.Retries
import java.util.concurrent.ConcurrentHashMap

// FIXME:
class NetworkRequestDispatcher(val maxConcurrentRequests: Int = 20) : AutoCloseable {
    private val requestCallbacks =
        ConcurrentHashMap<Any, Pair<(suspend (Exception) -> Unit)?, suspend (HttpResponse) -> Unit>>()

    private var client = HttpClient()

    private val semaphore = Semaphore(maxConcurrentRequests)

    var blocker = 0

    /**
     * @param onException NO REQUESTS ALLOWED INSIDE CALLBACK; may be executed from 0 up to [Retries] times
     * @param callback NO REQUESTS ALLOWED INSIDE CALLBACK
     */
    suspend fun get(
        urlString: String,
        httpRequestConfig: HttpRequestBuilder.() -> Unit = {},
        key: Any = urlString to httpRequestConfig,
        deduplicate: Boolean = true,
        retries: Int = Retries,
        onException: (suspend (Exception) -> Unit)? = null,
        callback: suspend (HttpResponse) -> Unit
    ) {
        if (deduplicate) {
            if (requestCallbacks.containsKey(key)) {
                println("duplicate url: $urlString")
                return
            }
            requestCallbacks[key] = onException to callback
        }

        var retriesLeft = retries
        while (retriesLeft != 0) {
            println("request url: $urlString")
            try {
                while (blocker >= maxConcurrentRequests) delay(10)
                blocker++
//        semaphore.withPermit {
                if (client.limitReached)
                    client = HttpClient() // FIXME: runs new client when previous is still running
                client.get(urlString, httpRequestConfig, { e ->
                    retriesLeft--
                    if (deduplicate) {
                        requestCallbacks[key]?.first?.invoke(e)
                        requestCallbacks.remove(key)
                    } else {
                        onException?.invoke(e)
                    }
                }) { response ->
                    retriesLeft = 0
                    if (deduplicate) {
                        requestCallbacks[key]?.second?.invoke(response)
                        requestCallbacks.remove(key)
                    } else {
                        callback.invoke(response)
                    }
                }
//        }
            } finally {
                blocker--
            }
            if (retriesLeft != 0)
                delay(500)
        }
    }

    override fun close() {
        client.close()
    }

    companion object {
        const val Retries = 3
    }
}