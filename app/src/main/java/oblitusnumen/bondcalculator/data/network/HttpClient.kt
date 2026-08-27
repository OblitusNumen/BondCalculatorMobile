package oblitusnumen.bondcalculator.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.Closeable

class HttpClient : Closeable {
    val client = HttpClient(CIO) {
        install(ContentNegotiation.Plugin) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
    }

    var limiter = 50

    val limitReached: Boolean
        get() = limiter == 0

    suspend fun get(
        urlString: String,
        httpRequestConfig: HttpRequestBuilder.() -> Unit = {},
        onException: (suspend (Exception) -> Unit)? = null,
        callback: suspend (HttpResponse) -> Unit
    ) {
        if (limiter <= 0)
            throw IllegalStateException("Limit of requests reached!")
        val limiter = --limiter
        try {
            val response = client.get(urlString, httpRequestConfig)
            callback.invoke(response)
        } catch (e: Exception) {
            e.printStackTrace()
            onException?.invoke(e)
            if (e is CancellationException) throw e
        } finally {
            if (limiter == 0)
                client.close()
        }
    }

    override fun close() {
        client.close()
    }
}