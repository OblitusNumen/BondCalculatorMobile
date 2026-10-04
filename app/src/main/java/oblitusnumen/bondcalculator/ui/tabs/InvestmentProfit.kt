package oblitusnumen.bondcalculator.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import oblitusnumen.bondcalculator.impl.InvestmentProfitCalculateResult
import oblitusnumen.bondcalculator.impl.calculateInvestmentProfit
import oblitusnumen.bondcalculator.impl.getSettings
import oblitusnumen.bondcalculator.impl.putSettings
import oblitusnumen.bondcalculator.ui.cursorToEnd
import oblitusnumen.bondcalculator.ui.format

@Composable
fun InvestmentProfitTab(paddingValues: PaddingValues) {
    // TODO:
    val context = LocalContext.current
    var settings by remember { mutableStateOf(getSettings(context)) }

    var initialDepositText: TextFieldValue by remember { mutableStateOf(TextFieldValue(settings.investmentInitialDeposit.format(2)).cursorToEnd()) }
    var monthlyDepositText: TextFieldValue by remember { mutableStateOf(TextFieldValue(settings.investmentMonthlyDeposit.format(2)).cursorToEnd()) }
    var rateText: TextFieldValue by remember { mutableStateOf(TextFieldValue(settings.investmentRatePercentage.format(4)).cursorToEnd()) }
    var yearsText: TextFieldValue by remember { mutableStateOf(TextFieldValue(settings.investmentYears.toString()).cursorToEnd()) }

    val calculateResult: () -> InvestmentProfitCalculateResult = { calculateInvestmentProfit(settings.taxPercentage, settings.inflationPercentage, settings.investmentRatePercentage, settings.investmentInitialDeposit, settings.investmentMonthlyDeposit, settings.investmentYears,) }

    var calculationResult: InvestmentProfitCalculateResult by remember { mutableStateOf(calculateResult()) }

    var calculationTarget: CalculationTarget by remember { mutableStateOf(CalculationTarget.Profit) }

    var totalReturnText: TextFieldValue by remember(calculationResult) { mutableStateOf(TextFieldValue(calculationResult.totalReturn.format(2)).cursorToEnd()) }
    var cleanReturnText: TextFieldValue by remember(calculationResult) { mutableStateOf(TextFieldValue(calculationResult.cleanReturn.format(2)).cursorToEnd()) }
    var cleanProfitText: TextFieldValue by remember(calculationResult) { mutableStateOf(TextFieldValue(calculationResult.cleanProfit.format(2)).cursorToEnd()) }
    var purchasingPowerText: TextFieldValue by remember(calculationResult) { mutableStateOf(TextFieldValue(calculationResult.purchasingPower.format(2)).cursorToEnd()) }

    LazyColumn(state = rememberLazyListState()) {
        item {
            Row(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                verticalAlignment = CenterVertically,
                horizontalArrangement = SpaceBetween
            ) {
                Text("Parameters", modifier = Modifier.weight(1f))
                Text("Function", modifier = Modifier.weight(.5f))
            }
        }

        // initial deposit
        item {
            Row(modifier = Modifier.padding(8.dp).fillParentMaxWidth(), verticalAlignment = CenterVertically, horizontalArrangement = SpaceBetween) {
                val currentFunction = calculationTarget == CalculationTarget.InitialDeposit
                TextField(
                    value = initialDepositText,
                    onValueChange = {
                        if (currentFunction)
                            return@TextField

                        val fieldValue = it.copy(it.text.replace(',', '.'))
                        try {
                            if (fieldValue.text.isNotEmpty()) {
                                settings = settings.copy(investmentInitialDeposit = fieldValue.text.toDouble())
                                putSettings(context, settings)
                                calculationResult = calculateResult()
                            }
                            initialDepositText = fieldValue
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp).padding(start = 12.dp, end = 6.dp).weight(1f),
                    label = { Text("Initial deposit") },
                    trailingIcon = { Text("₽") },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1,
                )

                RadioButton(currentFunction, { calculationTarget = CalculationTarget.InitialDeposit }, Modifier.weight(.5f))
            }
        }

        // monthly deposit
        item {
            Row(modifier = Modifier.padding(8.dp).fillParentMaxWidth(), verticalAlignment = CenterVertically, horizontalArrangement = SpaceBetween) {
                val currentFunction = calculationTarget == CalculationTarget.MonthlyDeposit
                TextField(
                    value = monthlyDepositText,
                    onValueChange = {
                        if (currentFunction)
                            return@TextField

                        val fieldValue = it.copy(it.text.replace(',', '.'))
                        try {
                            if (fieldValue.text.isNotEmpty()) {
                                settings = settings.copy(investmentMonthlyDeposit = fieldValue.text.toDouble())
                                putSettings(context, settings)
                                calculationResult = calculateResult()
                            }
                            monthlyDepositText = fieldValue
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp).padding(start = 12.dp, end = 6.dp).weight(1f),
                    label = { Text("Monthly deposit") },
                    trailingIcon = { Text("₽") },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1,
                )

                RadioButton(currentFunction, { calculationTarget = CalculationTarget.MonthlyDeposit }, Modifier.weight(.5f))
            }
        }

        // rate
        item {
            Row(modifier = Modifier.padding(8.dp).fillParentMaxWidth(), verticalAlignment = CenterVertically, horizontalArrangement = SpaceBetween) {
                val currentFunction = calculationTarget == CalculationTarget.Rate
                TextField(
                    value = rateText,
                    onValueChange = {
                        if (currentFunction)
                            return@TextField

                        val fieldValue = it.copy(it.text.replace(',', '.'))
                        try {
                            if (fieldValue.text.isNotEmpty()) {
                                settings = settings.copy(investmentRatePercentage = fieldValue.text.toDouble())
                                putSettings(context, settings)
                                calculationResult = calculateResult()
                            }
                            rateText = fieldValue
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp).padding(start = 12.dp, end = 6.dp).weight(1f),
                    label = { Text("Rate") },
                    trailingIcon = { Text("%") },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1,
                )

                RadioButton(currentFunction, { calculationTarget = CalculationTarget.Rate }, Modifier.weight(.5f))
            }
        }

        // span
        item {
            Row(modifier = Modifier.padding(8.dp).fillParentMaxWidth(), verticalAlignment = CenterVertically, horizontalArrangement = SpaceBetween) {
                TextField(
                    value = yearsText,
                    onValueChange = {
                        try {
                            if (it.text.isNotEmpty()) {
                                settings = settings.copy(investmentYears = it.text.toInt())
                                putSettings(context, settings)
                                calculationResult = calculateResult()
                            }
                            yearsText = it
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp).padding(start = 12.dp, end = 6.dp).weight(1f),
                    label = { Text("Span") },
                    trailingIcon = { Text("y") },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1,
                )

                Spacer(Modifier)
            }
        }

        // profit
        item {
            Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = CenterVertically, horizontalArrangement = SpaceBetween) {
                val currentFunction = calculationTarget == CalculationTarget.Profit
                Column(Modifier.weight(1f)) {
                    TextField(
                        value = totalReturnText,
                        onValueChange = {
                            if (currentFunction)
                                return@TextField

                            val fieldValue = it.copy(it.text.replace(',', '.'))
                            try {
                                if (fieldValue.text.isNotEmpty()) {
                                    // TODO: calculate profit and the function
//                                    settings = settings.copy(investmentMonthlyDeposit = fieldValue.text.toDouble())
//                                    putSettings(context, settings)
//                                    calculationResult = calculateResult()
                                }
                                //totalReturnText = fieldValue
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp).padding(start = 12.dp, end = 6.dp),
                        label = { Text("Total return") },
                        trailingIcon = { Text("₽") },
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        maxLines = 1,
                    )

                    TextField(
                        value = cleanReturnText,
                        onValueChange = {
                            if (currentFunction)
                                return@TextField

                            val fieldValue = it.copy(it.text.replace(',', '.'))
                            try {
                                if (fieldValue.text.isNotEmpty()) {
                                    // TODO: calculate profit and the function
//                                    settings = settings.copy(investmentMonthlyDeposit = fieldValue.text.toDouble())
//                                    putSettings(context, settings)
//                                    calculationResult = calculateResult()
                                }
                                //cleanReturnText = fieldValue
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp).padding(start = 12.dp, end = 6.dp),
                        label = { Text("Clean return") },
                        trailingIcon = { Text("₽") },
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        maxLines = 1,
                    )

                    TextField(
                        value = cleanProfitText,
                        onValueChange = {
                            if (currentFunction)
                                return@TextField

                            val fieldValue = it.copy(it.text.replace(',', '.'))
                            try {
                                if (fieldValue.text.isNotEmpty()) {
                                    // TODO: calculate profit and the function
//                                    settings = settings.copy(investmentMonthlyDeposit = fieldValue.text.toDouble())
//                                    putSettings(context, settings)
//                                    calculationResult = calculateResult()
                                }
                                //cleanProfitText = fieldValue
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp).padding(start = 12.dp, end = 6.dp),
                        label = { Text("Clean profit") },
                        trailingIcon = { Text("₽") },
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        maxLines = 1,
                    )

                    TextField(
                        value = purchasingPowerText,
                        onValueChange = {
                            if (currentFunction)
                                return@TextField

                            val fieldValue = it.copy(it.text.replace(',', '.'))
                            try {
                                if (fieldValue.text.isNotEmpty()) {
                                    // TODO: calculate profit and the function
//                                    settings = settings.copy(investmentMonthlyDeposit = fieldValue.text.toDouble())
//                                    putSettings(context, settings)
//                                    calculationResult = calculateResult()
                                }
                                purchasingPowerText = fieldValue
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp).padding(start = 12.dp, end = 6.dp),
                        label = { Text("Purchasing power") },
                        trailingIcon = { Text("₽") },
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        maxLines = 1,
                    )
                }

                RadioButton(currentFunction, { calculationTarget = CalculationTarget.Profit }, Modifier.weight(.5f))
            }
        }
    }
}

enum class CalculationTarget {
    Profit,
    Rate,
    InitialDeposit,
    MonthlyDeposit,
}