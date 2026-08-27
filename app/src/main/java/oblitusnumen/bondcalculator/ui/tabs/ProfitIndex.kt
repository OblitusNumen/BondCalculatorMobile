package oblitusnumen.bondcalculator.ui.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import oblitusnumen.bondcalculator.impl.ProfitCalculationResult
import oblitusnumen.bondcalculator.impl.calculateProfit
import oblitusnumen.bondcalculator.impl.getSettings
import oblitusnumen.bondcalculator.ui.*
import oblitusnumen.bondcalculator.ui.screen.MainScreenSettings
import java.time.LocalDate


@Composable
fun ProfitIndexTab(paddingValues: PaddingValues, mainScreenSettings: MainScreenSettings) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(getSettings(context)) }
    val datePicker = remember { DatePicker() }
    datePicker.TryCompose()

    var buyCostText: TextFieldValue by remember { mutableStateOf(TextFieldValue(mainScreenSettings.profitInvestmentCost.toString()).cursorToEnd()) }
    var totalReturnText: TextFieldValue by remember { mutableStateOf(TextFieldValue(mainScreenSettings.profitTotalReturn.toString()).cursorToEnd()) }
    val totalReturnTextFocusRequester = remember { FocusRequester() }

    val calculateResult: () -> ProfitCalculationResult =
        {
            calculateProfit(
                settings,
                mainScreenSettings.profitTaxed,
                mainScreenSettings.profitInvestmentCost,
                mainScreenSettings.profitTotalReturn,
                LocalDate.ofEpochDay(mainScreenSettings.profitInvestmentDate),
                LocalDate.ofEpochDay(mainScreenSettings.profitWithdrawDate)
            )
        }
    var calculationResult: ProfitCalculationResult by remember { mutableStateOf(calculateResult()) }

    LazyColumn(state = rememberLazyListState()) {
        item {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = CenterVertically) {
                Text("Is taxed: ", Modifier.padding(start = 8.dp))

                Switch(checked = mainScreenSettings.profitTaxed, onCheckedChange = {
                    mainScreenSettings.profitTaxed = it
                    calculationResult = calculateResult()
                }, Modifier.padding(horizontal = 8.dp))
            }
        }

        item {
            Row(verticalAlignment = CenterVertically) {
                OutlinedTextField(
                    value = buyCostText,
                    onValueChange = {
                        try {
                            if (it.text.isNotEmpty()) {
                                mainScreenSettings.profitInvestmentCost = it.text.toDouble()
                                calculationResult = calculateResult()
                            }
                            buyCostText = it
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp).padding(start = 12.dp, end = 6.dp).weight(1f),
                    label = { Text("Investment cost") },
                    trailingIcon = { Text("₽") },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { totalReturnTextFocusRequester.requestFocus() }),
                    maxLines = 1,
                )

                OutlinedTextField(
                    value = totalReturnText,
                    onValueChange = {
                        try {
                            if (it.text.isNotEmpty()) {
                                mainScreenSettings.profitTotalReturn = it.text.toDouble()
                                calculationResult = calculateResult()
                            }
                            totalReturnText = it
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp).padding(start = 6.dp, end = 12.dp).weight(1f)
                        .focusRequester(totalReturnTextFocusRequester),
                    label = { Text("Total return") },
                    trailingIcon = { Text("₽") },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1,
                )
            }
        }

        item {
            Row(
                modifier = Modifier.padding(4.dp).fillMaxWidth(),
                verticalAlignment = CenterVertically,
                horizontalArrangement = SpaceBetween
            ) {
                Text("Investment date: ", Modifier.padding(start = 8.dp))

                TextButton(onClick = {
                    datePicker.datePick({}, {
                        mainScreenSettings.profitInvestmentDate = it.toEpochDay()
                        calculationResult = calculateResult()
                    }, LocalDate.ofEpochDay(mainScreenSettings.profitInvestmentDate))
                }, Modifier.padding(horizontal = 8.dp)) {
                    Text(LocalDate.ofEpochDay(mainScreenSettings.profitInvestmentDate).toString())
                }
            }
        }

        item {
            Row(
                modifier = Modifier.padding(4.dp).fillMaxWidth(),
                verticalAlignment = CenterVertically,
                horizontalArrangement = SpaceBetween
            ) {
                Text("Withdraw date: ", Modifier.padding(start = 8.dp))

                TextButton(onClick = {
                    datePicker.datePick({}, {
                        mainScreenSettings.profitWithdrawDate = it.toEpochDay()
                        calculationResult = calculateResult()
                    }, LocalDate.ofEpochDay(mainScreenSettings.profitWithdrawDate))
                }, Modifier.padding(horizontal = 8.dp)) {
                    Text(LocalDate.ofEpochDay(mainScreenSettings.profitWithdrawDate).toString())
                }
            }
        }

//        item {
//            Box(Modifier.fillMaxWidth(), contentAlignment = Center) {
//                Button(onClick = {
//                    calculationResult = calculateResult()
//                }, Modifier.padding(horizontal = 8.dp).fillMaxWidth()) { Text("Calculate") }
//            }
//        }

        item {
            Text(
                "=============================\n" +
                        "Period:       ${calculationResult.period}\n" +
                        "Real profit rate: ${formatDoublePercentage(calculationResult.cleanProfitPercentage)}\n" +
                        "-----------------------------\n" +
                        "Total return:     ${formatRubbleValue(mainScreenSettings.profitTotalReturn)}\n" +
                        "  Clean return:   ${formatRubbleValue(calculationResult.cleanReturn)}\n" +
                        "    Clean profit: ${formatRubbleValue(calculationResult.cleanProfit)}\n" +
                        "    Cost:         ${formatRubbleValue(mainScreenSettings.profitInvestmentCost)}\n" +
                        "  Tax:            ${formatRubbleValue(calculationResult.taxValue)}\n" +
                        "=============================",
                Modifier.padding(horizontal = 8.dp), fontFamily = Monospace, fontSize = 12.sp
            )
        }

        item { Spacer(Modifier.height(paddingValues.calculateBottomPadding() + 64.dp)) }
    }
}