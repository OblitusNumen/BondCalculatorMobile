package oblitusnumen.bondcalculator.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import oblitusnumen.bondcalc.MoexInstrument
import oblitusnumen.bondcalc.moexapi.allInstruments
import oblitusnumen.bondcalculator.data.schema.Candle
import oblitusnumen.bondcalculator.data.schema.Security
import oblitusnumen.bondcalculator.ui.composition.LocalDataManager
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

@Composable
fun MarketTab(paddingValues: PaddingValues) {
// TODO: insert candles for non-trading days
    val random = Random(1L)
    val nDays = 50
    val candles = mutableListOf(
        Candle(
            "null", "null",
            LocalDate.now().minusDays(nDays.toLong() - 1).toEpochDay(),
            null, null, null,
            100.0, 100.0, 100.0, 100.0, 10.0,
            null, null, null, null
        )
    )
    repeat(nDays) {
        val prev = candles.last()
        val open = prev.close!! + (random.nextDouble() - .5) * 2
        val close = open + (random.nextDouble() - .5) * 10
        val min = minOf(open, close, (open + close) / 2 - random.nextDouble() * 5)
        val max = maxOf(open, close, (open + close) / 2 + random.nextDouble() * 5)
        candles.add(
            Candle(
                "null", "null",
                LocalDate.now().minusDays((nDays - it).toLong()).toEpochDay(),
                null, null, null,
                open,
                close, max, min, random.nextDouble() * 100,
                null, null, null, null
            )
        )
    }

    Column {
//        CandlestickChart(candles, Modifier.fillMaxWidth().weight(.3f))
//        BondScreen(Modifier.weight(1f))

        val coroutineScope = rememberCoroutineScope()
        LazyColumn(Modifier.weight(1f)) {
            item {
                var ratios: Map<String, Double> by remember { mutableStateOf(emptyMap()) }
                val dataManager = LocalDataManager.current
                val fetchRatios = suspend {
                    ratios =
                        dataManager.currencyRateRepository.getRates()?.mapValues { it.value.vUnitRate } ?: emptyMap()
                }

                Row(
                    Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Курсы конвертации")
                    IconButton(
                        { coroutineScope.launch { fetchRatios() } },
                        Modifier.padding(horizontal = 8.dp).fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                    }
                }

                Row(Modifier.padding(4.dp).fillMaxWidth()) {
                    Text("USD", Modifier.weight(1f))
                    Text("EUR", Modifier.weight(1f))
                    Text("CNY", Modifier.weight(1f))
                }
                val formatString = "%.4f"
                Row(Modifier.padding(4.dp).fillMaxWidth()) {
                    Text(ratios["USD"]?.let { formatString.format(it) } ?: "--.--", Modifier.weight(1f))
                    Text(ratios["EUR"]?.let { formatString.format(it) } ?: "--.--", Modifier.weight(1f))
                    Text(ratios["CNY"]?.let { formatString.format(it) } ?: "--.--", Modifier.weight(1f))
                }
                var rubDouble by rememberSaveable { mutableStateOf(100.0) }
                var rubString by rememberSaveable { mutableStateOf(formatString.format(rubDouble)) }
                var usdString by rememberSaveable(ratios) {
                    mutableStateOf(
                        formatString.format(
                            rubDouble / (ratios["USD"] ?: 1.0)
                        )
                    )
                }
                var eurString by rememberSaveable(ratios) {
                    mutableStateOf(
                        formatString.format(
                            rubDouble / (ratios["EUR"] ?: 1.0)
                        )
                    )
                }
                var cnyString by rememberSaveable(ratios) {
                    mutableStateOf(
                        formatString.format(
                            rubDouble / (ratios["CNY"] ?: 1.0)
                        )
                    )
                }

                //rub
                OutlinedTextField(
                    value = rubString,
                    onValueChange = {
                        try {
                            rubString = it.replace(',', '.')
                            rubDouble = rubString.toDouble()
                            usdString = formatString.format(rubDouble / (ratios["USD"] ?: 1.0))
                            eurString = formatString.format(rubDouble / (ratios["EUR"] ?: 1.0))
                            cnyString = formatString.format(rubDouble / (ratios["CNY"] ?: 1.0))
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).weight(1f),
                    trailingIcon = {
                        Text("₽")
                    },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1,
                )

                //usd
                OutlinedTextField(
                    value = usdString,
                    onValueChange = {
                        try {
                            usdString = it.replace(',', '.')
                            rubDouble = usdString.toDouble() * (ratios["USD"] ?: 1.0)
                            rubString = formatString.format(rubDouble)
                            eurString = formatString.format(rubDouble / (ratios["EUR"] ?: 1.0))
                            cnyString = formatString.format(rubDouble / (ratios["CNY"] ?: 1.0))
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).weight(1f),
                    trailingIcon = {
                        Text("$")
                    },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1,
                )

                //eur
                OutlinedTextField(
                    value = eurString,
                    onValueChange = {
                        try {
                            eurString = it.replace(',', '.')
                            rubDouble = eurString.toDouble() * (ratios["EUR"] ?: 1.0)
                            rubString = formatString.format(rubDouble)
                            usdString = formatString.format(rubDouble / (ratios["USD"] ?: 1.0))
                            cnyString = formatString.format(rubDouble / (ratios["CNY"] ?: 1.0))
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).weight(1f),
                    trailingIcon = {
                        Text("€")
                    },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1,
                )

                //cny
                OutlinedTextField(
                    value = cnyString,
                    onValueChange = {
                        try {
                            cnyString = it.replace(',', '.')
                            rubDouble = cnyString.toDouble() * (ratios["CNY"] ?: 1.0)
                            rubString = formatString.format(rubDouble)
                            usdString = formatString.format(rubDouble / (ratios["USD"] ?: 1.0))
                            eurString = formatString.format(rubDouble / (ratios["EUR"] ?: 1.0))
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).weight(1f),
                    trailingIcon = {
                        Text("¥")
                    },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1,
                )

                LaunchedEffect(ratios) {
                    if (ratios.isEmpty()) {
                        fetchRatios()
                    }
                }

                Spacer(Modifier.padding(4.dp))
            }

            repeat((allInstruments.size + 1) / 2) {
                item {
                    Row(
                        Modifier.padding(4.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Instrument(allInstruments[it * 2], Modifier.weight(1f))
                        Instrument(allInstruments[it * 2 + 1], Modifier.weight(1f))
                    }
                }
            }

            item {
                Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
            }
        }
    }
}

@Composable
fun Instrument(instrument: MoexInstrument, modifier: Modifier) {
    var candles: List<Candle>? by remember { mutableStateOf(null) }
    var now: Pair<Double?, Double?>? by remember { mutableStateOf(null) }

    Column(
        modifier.padding(horizontal = 4.dp)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
    ) {
        Text(instrument.name, Modifier.padding(horizontal = 4.dp))
        val change = now?.second
        Row(Modifier.padding(horizontal = 4.dp)) {
            Text(now?.first?.toString() ?: "--.--")
            Text(
                " ${change?.let { if (it < 0) "" else "+" } ?: ""}${change?.let { "%.2f%%".format(it) } ?: ""}",
                color = if (change == null || change == 0.0) Color.Gray else if (change > 0) Color.Green else Color.Red
            )
        }

        val dataManager = LocalDataManager.current
        LaunchedEffect(candles) {
            if (candles == null) {
                candles =
                    dataManager.candleRepository.getCandles(LocalDate.now().minusMonths(1), LocalDate.now(), instrument)
//                    MoexApiClient().use {
//                        candles =
//                            this.getCandles(instrument, LocalDate.now().minusMonths(1), LocalDate.now())
//                                .filter {
//                                    it.high != null && it.high != 0.0 &&
//                                            it.low != null && it.low != 0.0 &&
//                                            it.open != null && it.open != 0.0 &&
//                                            it.close != null && it.close != 0.0
//                                }
//                    }
            }
        }
        DisposableEffect(Unit) {
            val updateCallback: (Security?, LocalDateTime?) -> Unit = { security, updateTime ->
                now = security?.lastPrice to security?.lastToPrevPrcnt
            }

            dataManager.securityRepository.getSecuritySubscribe(instrument, updateCallback)

            onDispose {
                dataManager.securityRepository.getSecurityUnsubscribe(instrument, updateCallback)
            }
        }

        val coroutineScope = rememberCoroutineScope()
        Box(Modifier.padding(4.dp).height(100.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (candles == null) {
                IconButton(
                    {
                        coroutineScope.launch {
                            candles = dataManager.candleRepository.getCandles(
                                LocalDate.now().minusMonths(1),
                                LocalDate.now(),
                                instrument
                            )
                        }
                    }
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                }
            } else {
                CandlestickChart(candles!!, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
    candleWidth: Float = 12f,
    candleSpacing: Float = 8f
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("Нет данных")
        }

        return
    }

    val prices = candles.flatMap {
        listOfNotNull(it.high, it.low)
    }

    if (prices.isEmpty()) return

    val minPrice = prices.minOrNull() ?: return
    val maxPrice = prices.maxOrNull() ?: return

    val priceRange =
        (maxPrice - minPrice).takeIf { it > 0 }
            ?: 1.0

    Canvas(modifier = modifier.height(100.dp)) {

        val chartWidth = size.width
        val chartHeight = size.height

        val candleStep =
            candleWidth + candleSpacing

        /*
         * Пока показываем последние свечи,
         * которые помещаются на экран.
         */
        val visibleCount =
            (chartWidth / candleStep)
                .toInt()
                .coerceAtLeast(1)

        val visibleCandles =
            candles.takeLast(visibleCount)

        fun y(price: Double): Float {
            val normalized =
                ((price - minPrice) / priceRange)
                    .toFloat()

            return chartHeight -
                    normalized * chartHeight
        }

        visibleCandles.forEachIndexed { index, candle ->

            val x =
                index * candleStep +
                        candleWidth / 2f

            val open = candle.open ?: return@forEachIndexed
            val high = candle.high ?: return@forEachIndexed
            val low = candle.low ?: return@forEachIndexed
            val close = candle.close ?: return@forEachIndexed

            val yOpen = y(open)
            val yClose = y(close)
            val yHigh = y(high)
            val yLow = y(low)

            close >= open

            /*
             * Цвета здесь специально не фиксирую.
             * Можно вынести их в MaterialTheme.
             */
            val candleColor =
                if (close > open) {
                    Color.Green
                } else if (close < open) {
                    Color.Red
                } else {
                    Color.Gray
                }

            /*
             * Тень свечи
             */
            drawLine(
                color = candleColor,
                start = Offset(x, yHigh),
                end = Offset(x, yLow),
                strokeWidth = 1.dp.toPx()
            )

            /*
             * Тело свечи
             */
            val bodyTop =
                minOf(yOpen, yClose)

            val bodyBottom =
                maxOf(yOpen, yClose)

            val bodyHeight =
                maxOf(
                    bodyBottom - bodyTop,
                    1.dp.toPx()
                )

            drawRect(
                color = candleColor,
                topLeft = Offset(
                    x - candleWidth / 2f,
                    bodyTop
                ),
                size = Size(
                    candleWidth,
                    bodyHeight
                )
            )
        }
    }
}