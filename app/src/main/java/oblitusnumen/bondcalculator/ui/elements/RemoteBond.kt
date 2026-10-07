package oblitusnumen.bondcalculator.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import oblitusnumen.bondcalculator.data.schema.BondDetails
import oblitusnumen.bondcalculator.data.schema.BondPaymentType
import oblitusnumen.bondcalculator.data.schema.lastAmortization
import oblitusnumen.bondcalculator.data.schema.lastCoupon
import oblitusnumen.bondcalculator.ui.*
import java.time.LocalDateTime

@Composable
fun Bond(
    bond: BondDetails,
    updateTime: LocalDateTime?,
    calculateResult: BondDetails.CalculateResult?,
    isFavourite: Boolean? = null,
    onIsFavouriteUpdate: ((Boolean) -> Unit)? = null,
    openEditBond: (Int?) -> Unit?,
    onUpdateRequested: () -> Unit,
    onBondChange: (BondDetails) -> Unit
) {
    val currency = bond.bond.faceUnit!!
    Column(
        Modifier.clickable {
//            openEditBond(localBond.id)
        }.fillMaxWidth().padding(4.dp).background(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ).clip(RoundedCornerShape(8.dp)),
    ) {
        //update button
        if (updateTime != null)
            Box(Modifier.border(1.dp, Color.Transparent, RoundedCornerShape(8.dp)).padding(1.dp)) {
                Row(
                    Modifier.background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)).padding(4.dp)
                        .fillMaxWidth(),
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
//                    Text(updateTime.toLastUpdateString())
                    var updatedString by remember(updateTime) {
                        mutableStateOf(
                            LocalDateTime.now().toLastUpdateString(updateTime)
                        )
                    }
                    Text("Updated $updatedString")
                    LaunchedEffect(updateTime) {
                        while (true) {
                            delay(60 * 1000)
                            updatedString = LocalDateTime.now().toLastUpdateString(updateTime)
                        }
                    }

                    IconButton(
                        onUpdateRequested,
                        Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                    }
                }
            }

        //name
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(top = 4.dp),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (isFavourite != null) {
                IconButton(onClick = { onIsFavouriteUpdate?.invoke(!isFavourite) }) {
                    Icon(
                        if (isFavourite) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = null
                    )
                }
            }
            Text(bond.bond.shortname!!, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp).weight(1f))
            Text(
                calculateResult?.effectiveProfitRatePercentage?.let { formatDoublePercentage(it) } ?: "?",
                textAlign = TextAlign.End
            )
            var allParametersShown by remember { mutableStateOf(false) }
            IconButton(onClick = { allParametersShown = true }) {
                Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            if (allParametersShown) {
                AllParametersDialog(
                    bond,
                    calculateResult,
                ) { allParametersShown = false }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            //edit price
            var bondPriceText: TextFieldValue by remember {
                mutableStateOf(
                    TextFieldValue(
                        formatDouble(
                            bond.bondPricePrcnt,
                            4
                        )
                    ).cursorToEnd()
                )
            }
            OutlinedTextField(
                value = bondPriceText,
                onValueChange = {
                    val fieldValue = it.copy(it.text.replace(',', '.'))
                    try {
                        if (fieldValue.text.isNotEmpty()) {
                            onBondChange(bond.withPrice(fieldValue.text.toDouble()))
                        }
                        bondPriceText = fieldValue
                    } catch (_: Exception) {
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).weight(1f),
                label = @Composable { Text("Bond price") },
                trailingIcon = {
                    Text("%")
                },
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                maxLines = 1,
            )

            //coupons
            Column(Modifier.padding(horizontal = 2.dp), horizontalAlignment = CenterHorizontally) {
                Text("Coupon rate", fontSize = 8.sp)
                Text(calculateResult?.nominalCouponRate?.let { formatDoublePercentage(it) } ?: "?")
            }
            Column(Modifier.padding(horizontal = 2.dp), horizontalAlignment = CenterHorizontally) {
                Text("Total coupons", fontSize = 8.sp)
                Text(calculateResult?.couponCount?.toString() ?: "?")
            }
            var bondizationShown by remember { mutableStateOf(false) }
            IconButton(onClick = { bondizationShown = true }, Modifier.padding(horizontal = 2.dp)) {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (bondizationShown)
                BondizationDialog(calculateResult?.bondization, currency, { bondizationShown = false })
        }

        //result
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                ParameterRow(
                    "Cost",
                    calculateResult?.investmentCost?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Accrued",
                    calculateResult?.accrued?.let { formatCurrencyValue(currency, it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp),
                )
//                ParameterRow(
//                    "Commission",
//                    calculateResult?.buyCommission?.let { formatCurrencyValue(currency, it) } ?: "?",
//                    Modifier.padding(horizontal = 4.dp),
//                )
            }
            Column(Modifier.weight(1f)) {
//                ParameterRow(
//                    "Clean rate",
//                    calculateResult?.cleanProfitRatePercentage?.let { formatDoublePercentage(it) } ?: "?",
//                    Modifier.padding(horizontal = 4.dp),
//                )
//                ParameterRow(
//                    "Clean profit",
//                    calculateResult?.cleanProfit?.let { formatCurrencyValue(currency, it) } ?: "?",
//                    Modifier.padding(horizontal = 4.dp),
//                )
//                ParameterRow(
//                    "Effective profit",
//                    calculateResult?.effectiveProfit?.let { formatCurrencyValue(currency, it) } ?: "?",
//                    Modifier.padding(horizontal = 4.dp),
//                )
                ParameterRow(
                    "Maturity date",
                    bond.maturityDate?.toString() ?: "-",
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Duration",
                    calculateResult?.investmentPeriod?.let { formatPeriod(it) } ?: "?",
                    Modifier.padding(horizontal = 4.dp),
                )
            }
        }

        ParameterRow(
            "Coupon type",
            bond.bond.couponDetails ?: "-",
            Modifier.padding(horizontal = 12.dp),
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun AllParametersDialog(
    bond: BondDetails,
    calculateResult: BondDetails.CalculateResult?,
    onClose: () -> Unit
) {
    Dialog(
        onClose,
        DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(Modifier.background(MaterialTheme.colorScheme.background).padding(horizontal = 8.dp).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("All Parameters", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, contentDescription = null)
                }
            }

            LazyColumn(Modifier.fillMaxWidth()) {// TODO: add duration
                val padding = 48.dp
                val currency = bond.bond.faceUnit!!
                item {
                    SelectionContainer {
                        Text(bond.bond.secName!!)
                    }
                    SelectionContainer {
                        Text(
                            bond.bond.isin!!,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start,
                            fontFamily = Monospace,
                        )
                    }
                    ParameterRow(
                        "Currency",
                        "${currency}/${currencySign(currency)}",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Initial value",
                        calculateResult?.bondization?.lastAmortization()
                            ?.let {
                                formatCurrencyValue(
                                    currency,
                                    it.valuePercent / 100f * it.value / calculateResult.numberOfLots
                                )
                            }
                            ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Face value",
                        calculateResult?.faceValue?.let { formatCurrencyValue(currency, it) } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Price",
                        formatCurrencyValue(currency, bond.bondPrice),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Coupon",
                        calculateResult?.numberOfLots?.let {
                            formatCurrencyValue(
                                currency,
                                (calculateResult.bondization.lastCoupon()?.value ?: 0.0) / it
                            )
                        } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Coupon period",
                        "${bond.bond.couponPeriod ?: 0}d",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Investment date",
                        calculateResult?.investmentDate?.toString() ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Return date",
                        bond.maturityDate?.toString() ?: "-",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Investment span",
                        calculateResult?.investmentPeriod?.let { "${formatPeriod(it)}${if (it > 30) " / ${it}d" else ""}" }
                            ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Bond type",
                        bond.bond.bondType ?: "-",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Bond subtype",
                        bond.bond.bondSubType ?: "-",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Coupons details",
                        bond.bond.couponDetails ?: "-",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Face value type",
                        bond.bond.faceValueType ?: "-",
                        Modifier.padding(horizontal = padding)
                    )
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Efficiency")
                    ParameterRow(
                        "Price rate",
                        calculateResult?.priceRatePercentage?.let { formatDoublePercentage(it) } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    EfficiencyTable(calculateResult?.toLocalCalculateResult(), currency)
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Investment cost")
                    ParameterRow(
                        "Investment cost",
                        calculateResult?.investmentCost?.let { formatCurrencyValue(currency, it) } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Bond price",
                        calculateResult?.bondPrice?.let { formatCurrencyValue(currency, it) } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Accrued",
                        calculateResult?.accrued?.let { formatCurrencyValue(currency, it) } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Commission",
                        calculateResult?.buyCommission?.let { formatCurrencyValue(currency, it) } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Coupons")
                    ParameterRow(
                        "Nominal rate",
                        calculateResult?.nominalCouponRate?.let { formatDoublePercentage(it) } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Coupon amount",
                        calculateResult?.bondization.lastCoupon()?.value?.let { formatCurrencyValue(currency, it) }
                            ?: "-",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Coupon count",
                        calculateResult?.couponCount?.let { "$it" } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Total coupon value",
                        calculateResult?.totalCouponValue?.let { formatCurrencyValue(currency, it) } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Next coupon",
                        calculateResult?.bondization?.firstOrNull { it.type == BondPaymentType.Coupon }?.date?.toString()
                            ?: "-",
                        Modifier.padding(horizontal = padding)
                    )
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Calculation parameters")
                    ParameterRow(
                        "Tax",
                        calculateResult?.tax?.let { formatDoublePercentage(it) } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Commission",
                        calculateResult?.commissionPercentage?.let { "${it.toSigFigString()}%" } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Deposit reinvestment rate",
                        calculateResult?.depositReinvestmentRatePercentage?.let { "$it%" } ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Lots",
                        calculateResult?.numberOfLots?.toString() ?: "?",
                        Modifier.padding(horizontal = padding)
                    )
                }
            }
        }
    }
}