package oblitusnumen.bondcalculator.data.repository

import io.ktor.client.call.*
import io.ktor.client.request.*
import oblitusnumen.bondcalculator.data.database.dao.CurrencyRateDao
import oblitusnumen.bondcalculator.data.network.NetworkRequestDispatcher
import oblitusnumen.bondcalculator.data.schema.CurrencyRate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CurrencyRateRepository(
    private val dao: CurrencyRateDao,
    private val dispatcher: NetworkRequestDispatcher,
) {

    suspend fun getRates(
        date: LocalDate? = null,
    ): Map<String, CurrencyRate>? {
        val now = LocalDate.now()
        val date = date ?: now
        if (date == now && dao.getCachedDate() == now) {
            return dao.getRates()!!
        }

        val formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy")

        val dateString =
            date.format(formatter)

        var xml: String? = null
        dispatcher.get(
            "https://www.cbr.ru/scripts/XML_daily.asp",
            {
                parameter(
                    "date_req",
                    dateString
                )
            },
            "rates:$date"
        ) { response ->
            xml = response.body()
        }

        if (xml == null)
            return null

        val rates = parseRates(xml!!)
        dao.putCurrencies(rates, date)
        return rates
    }

    private fun parseRates(
        xml: String
    ): Map<String, CurrencyRate> {

        /*
         * Для production лучше использовать
         * XML parser, например javax.xml.parsers.
         */

        val result = mutableMapOf<String, CurrencyRate>()

        val dateRegex =
            Regex(
                "<ValCurs Date=\"(.*?)\"",
                RegexOption.DOT_MATCHES_ALL
            )

        val dateEpochDay = dateRegex.find(xml)!!.let {
            LocalDate.parse(it.groups[1]!!.value, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }.toEpochDay()

        val regex =
            Regex(
                ("<Valute ID=\"(.*?)\">.*?" +
                        "<NumCode>(.*?)</NumCode>.*?" +
                        "<CharCode>(.*?)</CharCode>.*?" +
                        "<Nominal>(.*?)</Nominal>.*?" +
                        "<Name>(.*?)</Name>.*?" +
                        "<Value>(.*?)</Value>.*?" +
                        "<VunitRate>(.*?)</VunitRate>.*?" +
                        "</Valute>").trimIndent(),
                RegexOption.DOT_MATCHES_ALL
            )

        regex.findAll(xml)
            .forEach { match ->
                val id =
                    match.groupValues[1]

                val numCode =
                    match.groupValues[2]

                val charCode =
                    match.groupValues[3]

                val nominal =
                    match.groupValues[4]
                        .toInt()

                val name =
                    match.groupValues[5]

                val value =
                    match.groupValues[6]
                        .replace(',', '.')
                        .toDouble()

                val vUnitRate =
                    match.groupValues[7]
                        .replace(',', '.')
                        .toDouble()

                result[charCode] =
                    CurrencyRate(
                        id = id,
                        numCode = numCode,
                        charCode = charCode,
                        nominal = nominal,
                        name = name,
                        value = value,
                        vUnitRate = vUnitRate,
                        dateEpochDay = dateEpochDay,
                    )
            }

        return result
    }
}