package oblitusnumen.bondcalculator.ui.tabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import oblitusnumen.bondcalc.MoexInstrument
import oblitusnumen.bondcalc.moexapi.CNY_RUB
import oblitusnumen.bondcalc.moexapi.GLD
import oblitusnumen.bondcalc.moexapi.IMOEX
import oblitusnumen.bondcalc.moexapi.USD_RUB
import oblitusnumen.bondcalculator.data.network.RemoteDataStatus
import oblitusnumen.bondcalculator.data.schema.Candle
import oblitusnumen.bondcalculator.data.schema.Security
import oblitusnumen.bondcalculator.ui.composition.LocalDataManager
import oblitusnumen.bondcalculator.ui.test.BondScreen
import java.time.LocalDate
import kotlin.random.Random

@Composable
fun MarketTab(paddingValues: PaddingValues) {
// TODO: insert candles for non-trading days
    val random = Random(1L)
    val nDays = 50
    val candles = mutableListOf(
        Candle(
            "null", "null",
            LocalDate.now().minusDays(nDays.toLong() - 1),
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
                LocalDate.now().minusDays((nDays - it).toLong()),
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
                Row(Modifier.padding(4.dp).fillMaxWidth()) {
                    Text(ratios["USD"]?.let { "%.4f".format(it) } ?: "--.--", Modifier.weight(1f))
                    Text(ratios["EUR"]?.let { "%.4f".format(it) } ?: "--.--", Modifier.weight(1f))
                    Text(ratios["CNY"]?.let { "%.4f".format(it) } ?: "--.--", Modifier.weight(1f))
                }

                LaunchedEffect(ratios) {
                    if (ratios.isEmpty()) {
                        fetchRatios()
                    }
                }

                Spacer(Modifier.padding(4.dp))
            }

            item {
                Row(
                    Modifier.padding(4.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Instrument(IMOEX, Modifier.weight(1f))
                    Instrument(GLD, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    Modifier.padding(4.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Instrument(USD_RUB, Modifier.weight(1f))
                    Instrument(CNY_RUB, Modifier.weight(1f))
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
    val coroutineScope = rememberCoroutineScope()
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
            val updateCallback: (Security?, RemoteDataStatus) -> Unit = { security, status ->
                now = security?.lastPrice to security?.lastToPrevPrcnt
            }

            dataManager.securityRepository.getSecuritySubscribe(instrument, updateCallback)

            onDispose {
                dataManager.securityRepository.getSecurityUnsubscribe(instrument, updateCallback)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            candles?.let { CandlestickChart(it, Modifier.padding(4.dp).fillMaxWidth()) }
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

            val bullish = close >= open

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