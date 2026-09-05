package oblitusnumen.bondcalculator.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import oblitusnumen.bondcalculator.impl.*
import oblitusnumen.bondcalculator.ui.*


@Composable
fun DepositsTab(paddingValues: PaddingValues) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(getSettings(context)) }
    val datePicker = remember { DatePicker() }
    datePicker.TryCompose()

    // TODO: save strings instead of double
    var deposit: Double by remember { mutableDoubleStateOf(getDepositValue(context)) }
    var rate: Double by remember { mutableDoubleStateOf(getDepositRate(context)) }
    var period: Int by remember { mutableIntStateOf(getDepositPeriod(context)) }

    var depositText: TextFieldValue by remember {
        mutableStateOf(TextFieldValue(formatDouble(deposit)).cursorToEnd())
    }
    var rateText: TextFieldValue by remember { mutableStateOf(TextFieldValue(formatDouble(rate, 4)).cursorToEnd()) }
    val rateTextFocusRequester = remember { FocusRequester() }
    var periodText: TextFieldValue by remember { mutableStateOf(TextFieldValue(period.toString()).cursorToEnd()) }
    val periodTextFocusRequester = remember { FocusRequester() }

    val calculateResult: () -> ProfitCalculationResult =
        { calculateDepositProfit(settings, rate, deposit, period) }
    var calculationResult: ProfitCalculationResult by remember { mutableStateOf(calculateResult()) }

    LazyColumn(state = rememberLazyListState()) {
        addSetting(
            "Deposit", depositText, {
                try {
                    if (it.text.isNotEmpty()) {
                        deposit = it.text.toDouble()
                        setDepositValue(context, deposit)
                        calculationResult = calculateResult()
                    }
                    depositText = it
                } catch (_: Exception) {
                }
            },
            keyboardType = KeyboardType.Decimal,
            focusRequester = null,
            nextFocusRequester = rateTextFocusRequester
        ) {
            Text("₽")
        }

        item {
            Row(verticalAlignment = CenterVertically) {
                OutlinedTextField(
                    value = rateText,
                    onValueChange = {
                        val fieldValue = it.copy(it.text.replace(',', '.'))
                        try {
                            if (fieldValue.text.isNotEmpty()) {
                                rate = fieldValue.text.toDouble()
                                setDepositRate(context, rate)
                                calculationResult = calculateResult()
                            }
                            rateText = fieldValue
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp).padding(start = 12.dp, end = 6.dp).weight(1f)
                        .focusRequester(rateTextFocusRequester),
                    label = { Text("Rate") },
                    trailingIcon = { Text("%") },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { periodTextFocusRequester.requestFocus() }),
                    maxLines = 1,
                )

                OutlinedTextField(
                    value = periodText,
                    onValueChange = {
                        val fieldValue = it.copy(it.text.replace(',', '.'))
                        try {
                            if (fieldValue.text.isNotEmpty()) {
                                period = fieldValue.text.toInt()
                                setDepositPeriod(context, period)
                                calculationResult = calculateResult()
                            }
                            periodText = fieldValue
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.padding(vertical = 4.dp).padding(start = 6.dp, end = 12.dp).weight(1f)
                        .focusRequester(periodTextFocusRequester),
                    label = { Text("Period") },
                    trailingIcon = { Text("d") },
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1,
                )
            }
        }

        item {
            Column(
                Modifier.padding(vertical = 16.dp, horizontal = 32.dp).fillMaxWidth(),
                horizontalAlignment = CenterHorizontally
            ) {
                ParameterRow(
                    "Real profit rate",
                    formatDoublePercentage(calculationResult.cleanProfitPercentage),
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Total return",
                    formatRubbleValue(calculationResult.totalReturn),
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Profit",
                    formatRubbleValue(calculationResult.cleanProfit),
                    Modifier.padding(horizontal = 4.dp),
                )
            }
        }

        item {
            Row(verticalAlignment = CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    "Period",
                    Modifier.padding(2.dp).weight(1f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace
                )
                Text(
                    "Real profit rate",
                    Modifier.padding(2.dp).weight(1f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace,
                )
                Text(
                    "Clean profit",
                    Modifier.padding(2.dp).weight(1f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = Monospace
                )
            }
        }

        DEPOSIT_COMMON_PERIODS.forEachIndexed { index, period ->
            item {
                val result = remember(calculationResult) { calculateDepositProfit(settings, rate, deposit, period) }

                Row(
                    verticalAlignment = CenterVertically,
                    modifier = (if (index % 2 == 0) Modifier.background(Color.Gray.copy(alpha = 0.1f))
                    else Modifier).padding(horizontal = 8.dp)
                ) {
                    Text(
                        "${formatPeriod(result.period)} / ${result.period}d",
                        Modifier.padding(2.dp).weight(1f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        fontFamily = Monospace,
                    )
                    Text(
                        formatDoublePercentage(result.cleanProfitPercentage),
                        Modifier.padding(2.dp).weight(1f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        fontFamily = Monospace,
                    )
                    Text(
                        formatRubbleValue(result.cleanProfit),
                        Modifier.padding(2.dp).weight(1f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        fontFamily = Monospace,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(paddingValues.calculateBottomPadding() + 64.dp)) }
    }
}