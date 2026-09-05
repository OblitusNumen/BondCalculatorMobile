package oblitusnumen.bondcalculator.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CbrClient : AutoCloseable {
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation.Plugin) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }
    }

    suspend fun getRates(
        date: LocalDate
    ): Map<String, BigDecimal> {
        val formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy")

        val dateString =
            date.format(formatter)

        val xml: String =
            client.get(
                "https://www.cbr.ru/scripts/XML_daily.asp"
            ) {
                parameter(
                    "date_req",
                    dateString
                )
            }.body()

        return parseRates(xml)
    }

    private fun parseRates(
        xml: String
    ): Map<String, BigDecimal> {

        /*
         * Для production лучше использовать
         * XML parser, например javax.xml.parsers.
         */

        val result =
            mutableMapOf<String, BigDecimal>()

        val regex =
            Regex(
                "<Valute.*?<CharCode>(.*?)</CharCode>.*?<Nominal>(.*?)</Nominal>.*?<Value>(.*?)</Value>.*?</Valute>".trimIndent(),
                RegexOption.DOT_MATCHES_ALL
            )

        regex.findAll(xml)
            .forEach { match ->
                val charCode =
                    match.groupValues[1]

                val nominal =
                    match.groupValues[2]
                        .toInt()

                val value =
                    match.groupValues[3]
                        .replace(',', '.')
                        .toBigDecimal()

                result[charCode] =
                    value.divide(
                        nominal.toBigDecimal(),
                        10,
                        RoundingMode.HALF_UP
                    )
            }

        return result
    }

    suspend fun use(onException: ((Exception) -> Unit)? = null, block: suspend CbrClient.() -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
            onException?.invoke(e)
            if (e is CancellationException) throw e
        } finally {
            close()
        }
    }

    override fun close() = client.close()
}