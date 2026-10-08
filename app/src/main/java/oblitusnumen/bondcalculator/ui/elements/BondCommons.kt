package oblitusnumen.bondcalculator.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import oblitusnumen.bondcalculator.data.schema.BondPayment
import oblitusnumen.bondcalculator.data.schema.BondPaymentType
import oblitusnumen.bondcalculator.data.schema.LocalBond
import oblitusnumen.bondcalculator.ui.formatCurrencyValue
import oblitusnumen.bondcalculator.ui.formatDoublePercentage

@Composable
fun BondizationDialog(
    bondization: List<BondPayment>?,
    currency: String = "RUR",
    onClose: () -> Unit
) {
    Dialog(
        onClose,
        DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = 8.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Payments", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, contentDescription = null)
                }
            }

            LazyColumn(state = rememberLazyListState()) {
                bondization?.forEachIndexed { index, bondization ->
                    item {
                        Row(
                            verticalAlignment = CenterVertically,
                            modifier = (if (index % 2 == 0)
                                Modifier.background(Color.Gray.copy(alpha = 0.1f))
                            else
                                Modifier).fillMaxWidth().padding(4.dp).padding(end = 12.dp)
                        ) {
                            Text(
                                if (bondization.isAmortization) "Amortization" else "Coupon",
                                Modifier.padding(2.dp).weight(1f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.End
                            )
                            Text(
                                bondization.date.toString(),
                                Modifier.padding(2.dp).weight(1f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.End
                            )
                            Text(
                                bondization.value?.let { formatCurrencyValue(currency, it) } ?: "-",
                                Modifier.padding(2.dp).weight(1f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.End
                            )
                            Text(
                                bondization.valuePercent?.let { formatDoublePercentage(it, 2) } ?: "-",
                                Modifier.padding(2.dp).weight(1f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                } ?: item {
                    Text(text = "Loading...", Modifier.padding(2.dp))
                }
            }
        }
    }
}

@Composable
fun EfficiencyTable(calculateResult: LocalBond.CalculateResult?, currency: String = "RUR") {
    LazyRow(state = rememberLazyListState()) {
        item {
            Column(Modifier.width(IntrinsicSize.Max)) {
                Text(
                    "Parameter",
                    Modifier.background(MaterialTheme.colorScheme.onBackground.copy(alpha = .2f))
                        .padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace
                )
                Text(
                    "Profit rate",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Start,
                    fontFamily = Monospace
                )
                Text(
                    "Profit",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Start,
                    fontFamily = Monospace
                )
                Text(
                    "Reinvestment profit",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Start,
                    fontFamily = Monospace
                )
                Text(
                    "Reinvestment profit %",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Start,
                    fontFamily = Monospace
                )
                Text(
                    "Return",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Start,
                    fontFamily = Monospace
                )
            }
        }
        item {
            Column(Modifier.width(IntrinsicSize.Max)) {
                Text(
                    "Effective",
                    Modifier.background(MaterialTheme.colorScheme.onBackground.copy(alpha = .2f))
                        .padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace
                )
                Text(
                    calculateResult?.effectiveProfitRatePercentage?.let { formatDoublePercentage(it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.effectiveProfit?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.reinvestProfit?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.effectiveProfitReinvestPercentage?.let { formatDoublePercentage(it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.effectiveReturn?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
            }
        }
        item {
            Column(Modifier.width(IntrinsicSize.Max)) {
                Text(
                    "Clean",
                    Modifier.background(MaterialTheme.colorScheme.onBackground.copy(alpha = .2f))
                        .padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace
                )
                Text(
                    calculateResult?.cleanProfitRatePercentage?.let { formatDoublePercentage(it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.cleanProfit?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    "-",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace,
                )
                Text(
                    "-",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.cleanReturn?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
            }
        }
        item {
            Column(Modifier.width(IntrinsicSize.Max)) {
                Text(
                    "Untaxed",
                    Modifier.background(MaterialTheme.colorScheme.onBackground.copy(alpha = .2f))
                        .padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.totalProfitRatePercentage?.let { formatDoublePercentage(it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.totalProfit?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    "-",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace,
                )
                Text(
                    "-",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.totalReturn?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
            }
        }
        item {
            Column(Modifier.width(IntrinsicSize.Max)) {
                Text(
                    "Deposit",
                    Modifier.background(MaterialTheme.colorScheme.onBackground.copy(alpha = .2f))
                        .padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace
                )
                Text(
                    calculateResult?.depositEffectiveProfitRatePercentage?.let { formatDoublePercentage(it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.depositEffectiveProfit?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.depositReinvestProfit?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.depositEffectiveProfitReinvestPercentage?.let { formatDoublePercentage(it) }
                        ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    calculateResult?.depositEffectiveReturn?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
            }
        }
    }
}