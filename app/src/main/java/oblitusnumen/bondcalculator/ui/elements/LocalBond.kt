package oblitusnumen.bondcalculator.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import oblitusnumen.bondcalculator.data.schema.BondPaymentType
import oblitusnumen.bondcalculator.data.schema.FinanceParameters
import oblitusnumen.bondcalculator.data.schema.LocalBond
import oblitusnumen.bondcalculator.data.schema.lastCoupon
import oblitusnumen.bondcalculator.ui.*
import java.time.LocalDate

fun LazyListScope.bond(
    key: Any,
    bond: LocalBond,
    settings: FinanceParameters,
    hasCommission: Boolean,
    isTaxed: Boolean,
    investmentDate: LocalDate,
    numberOfLots: Int,
    bonds: List<LocalBond>,
    openEditBond: (Int?) -> Unit,
    onBondSave: (LocalBond) -> Unit
) {
    item(key = key) {
        val calculateResult by remember(settings, hasCommission, isTaxed, investmentDate, numberOfLots, bonds) {
            mutableStateOf(
                bond.calculateProfit(
                    settings,
                    hasCommission,
                    isTaxed,
                    investmentDate,
                    numberOfLots
                )
            )
        }

        Bond(bond, calculateResult, null, null, openEditBond, onBondSave)
    }
}

@Composable
fun Bond(
    bond: LocalBond,
    calculateResult: LocalBond.CalculateResult,
    isFavourite: Boolean? = null,
    onIsFavouriteUpdate: ((Boolean) -> Unit)? = null,
    openEditBond: (Int?) -> Unit?,
    onBondUpdate: (LocalBond) -> Unit
) {
    Column(
        Modifier.clickable {
            openEditBond(bond.id)
        }.fillMaxWidth().padding(4.dp).background(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ).clip(RoundedCornerShape(8.dp)),
    ) {
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
            Text(bond.name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp).weight(1f))
            val value = calculateResult.effectiveProfitRatePercentage
            Text(
                formatDoublePercentage(value),
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
                            onBondUpdate(bond.copy(bondPricePrcnt = fieldValue.text.toDouble()))
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
                Text(formatDoublePercentage(calculateResult.nominalCouponRate))
            }
            Column(Modifier.padding(horizontal = 2.dp), horizontalAlignment = CenterHorizontally) {
                Text("Total coupons", fontSize = 8.sp)
                Text(calculateResult.couponCount.toString())
            }
            var bondizationShown by remember { mutableStateOf(false) }
            IconButton(onClick = { bondizationShown = true }, Modifier.padding(horizontal = 2.dp)) {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            // FIXME: add currency
            if (bondizationShown) {
                BondizationDialog(calculateResult.bondization, "RUR", { bondizationShown = false })
            }
        }

        //result
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                ParameterRow(
                    "Cost",
                    formatRubbleValue(calculateResult.investmentCost),
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Accrued",
                    formatRubbleValue(calculateResult.accrued),
                    Modifier.padding(horizontal = 4.dp),
                )
//                ParameterRow(
//                    "Commission",
//                    formatRubbleValue(calculateResult.buyCommission),
//                    Modifier.padding(horizontal = 4.dp),
//                )
            }
            Column(Modifier.weight(1f)) {
//                ParameterRow(
//                    "Clean profit",
//                    formatDoublePercentage(calculateResult.cleanProfitRatePercentage),
//                    Modifier.padding(horizontal = 4.dp),
//                )
//                ParameterRow(
//                    "Clean profit",
//                    formatRubbleValue(calculateResult.cleanProfit),
//                    Modifier.padding(horizontal = 4.dp),
//                )
//                ParameterRow(
//                    "Effective profit",
//                    formatRubbleValue(calculateResult.effectiveProfit),
//                    Modifier.padding(horizontal = 4.dp),
//                )
                ParameterRow(
                    "Maturity date",
                    bond.maturityDate.toString(),
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Duration",
                    formatPeriod(calculateResult.investmentPeriod),
                    Modifier.padding(horizontal = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun AllParametersDialog(
    bond: LocalBond,
    calculateResult: LocalBond.CalculateResult,
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
                item {
                    Text(bond.name)
                    ParameterRow(
                        "Value",
                        formatRubbleValue(bond.bondValue),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Price",
                        formatRubbleValue(bond.bondPrice),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Coupon",
                        formatRubbleValue(bond.couponValue),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Investment date",
                        calculateResult.investmentDate.toString(),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Maturity date",
                        bond.maturityDate.toString(),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Investment span",
                        "${formatPeriod(calculateResult.investmentPeriod)}${if (calculateResult.investmentPeriod > 30) " / ${calculateResult.investmentPeriod}d" else ""}",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Reinvest accrued",
                        formatRubbleValue(calculateResult.reinvestAccrued),
                        Modifier.padding(horizontal = padding)
                    )
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Efficiency")
                    ParameterRow(
                        "Price rate",
                        formatDoublePercentage(calculateResult.priceRatePercentage),
                        Modifier.padding(horizontal = padding)
                    )
                    EfficiencyTable(calculateResult)
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Investment cost")
                    ParameterRow(
                        "Investment cost",
                        formatRubbleValue(calculateResult.investmentCost),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Bond price",
                        formatRubbleValue(calculateResult.bondPrice),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Accrued",
                        formatRubbleValue(calculateResult.accrued),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Commission",
                        formatRubbleValue(calculateResult.buyCommission),
                        Modifier.padding(horizontal = padding)
                    )
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Coupons")
                    ParameterRow(
                        "Nominal rate",
                        formatDoublePercentage(calculateResult.nominalCouponRate),
                        Modifier.padding(horizontal = padding)
                    )
                    // FIXME: add currency
                    ParameterRow(
                        "Coupon amount",
                        calculateResult.bondization.lastCoupon()?.value?.let { formatRubbleValue(it) } ?: "-",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Coupon count",
                        "${calculateResult.couponCount}",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Total coupon value",
                        formatRubbleValue(calculateResult.totalCouponValue),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Reinvest Accrued",
                        formatRubbleValue(calculateResult.reinvestAccrued),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Next coupon",
                        calculateResult.bondization.firstOrNull { it.type == BondPaymentType.Coupon }?.date?.toString()
                            ?: "-",
                        Modifier.padding(horizontal = padding)
                    )
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Calculation parameters")
                    ParameterRow(
                        "Tax",
                        formatDoublePercentage(calculateResult.tax),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Commission",
                        "${calculateResult.commissionPercentage.toSigFigString()}%",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Deposit reinvestment rate",
                        "${calculateResult.depositReinvestmentRatePercentage}%",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Lots",
                        calculateResult.numberOfLots.toString(),
                        Modifier.padding(horizontal = padding)
                    )
                }
            }
        }
    }
}