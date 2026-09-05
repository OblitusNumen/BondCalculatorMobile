package oblitusnumen.bondcalculator.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import oblitusnumen.bondcalculator.data.schema.CashFlow
import oblitusnumen.bondcalculator.impl.getXirrCashFlows
import oblitusnumen.bondcalculator.impl.saveXirrCashFlows
import oblitusnumen.bondcalculator.impl.xirr
import oblitusnumen.bondcalculator.ui.ParameterRow
import oblitusnumen.bondcalculator.ui.formatDouble
import oblitusnumen.bondcalculator.ui.formatDoublePercentage
import oblitusnumen.bondcalculator.ui.rememberDatePicker
import java.time.LocalDate

@Composable
fun XirrTab(paddingValues: PaddingValues) {
    val context = LocalContext.current
    var cashFlows by remember { mutableStateOf(getXirrCashFlows(context)) }
    var xirr by remember { mutableStateOf(0.0) }

    LaunchedEffect(cashFlows) {
        try {
            xirr = xirr(cashFlows) * 100
        } catch (_: Exception) {
            xirr = 0.0
        }
    }

    LazyColumn(state = rememberLazyListState()) {
        item {
            ParameterRow(
                "XIRR",
                formatDoublePercentage(xirr, 4),
                Modifier.padding(horizontal = 40.dp)
            )
        }

        item {
            var cashFlow by remember { mutableStateOf(CashFlow(0.0, LocalDate.now().toEpochDay())) }
            var newCashFlowShown by remember { mutableStateOf(false) }
            Box(Modifier.clickable { newCashFlowShown = true }.fillMaxWidth(), contentAlignment = Alignment.Center) {
                IconButton(onClick = { newCashFlowShown = true }) {
                    Icon(Icons.Default.Add, null)
                }
            }
            if (newCashFlowShown) {
                AlertDialog(onDismissRequest = { newCashFlowShown = false }, confirmButton = {
                    TextButton(onClick = {
                        newCashFlowShown = false
                        cashFlows = cashFlows.toMutableList().apply { add(cashFlow) }.sortedBy { it.dateEpochDay }
                        saveXirrCashFlows(context, cashFlows)
                    }) {
                        Text("Ok")
                    }
                }, title = { Text("Add cash flow") }, text = {
                    CashFlow(cashFlow, { cashFlow = it }, {
                        newCashFlowShown = false
                    }, false)
                })
            }
        }

        cashFlows.reversed().forEachIndexed { index, cashFlow ->
            val index = cashFlows.size - index - 1
            item {
                CashFlow(
                    cashFlow,
                    {
                        cashFlows = cashFlows.toMutableList().apply { this[index] = it }.sortedBy { it.dateEpochDay }
                        saveXirrCashFlows(context, cashFlows)
                    },
                    {
                        cashFlows = cashFlows.toMutableList().apply { removeAt(index) }.toList()
                        saveXirrCashFlows(context, cashFlows)
                    })
            }
        }

        item { Spacer(Modifier.height(paddingValues.calculateBottomPadding() + 64.dp)) }
    }
}

@Composable
fun CashFlow(
    cashFlow: CashFlow,
    onCashFlowChange: (CashFlow) -> Unit,
    onRemove: () -> Unit,
    interactable: Boolean = true
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (interactable)
            IconButton(onClick = { onCashFlowChange(cashFlow.copy(amount = -cashFlow.amount)) }) {
                Icon(
                    if (cashFlow.amount > 0) Icons.Rounded.Add else Icons.Rounded.Remove,
                    null,
                    modifier = Modifier.size(20.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .2f), CircleShape),
                    tint = if (cashFlow.amount > 0) Color.Green else if (cashFlow.amount < 0) Color.Red else Color.Gray
                )
            }

        OutlinedTextField(
            value = formatDouble(cashFlow.amount, 2),
            onValueChange = {
                try {
                    onCashFlowChange(cashFlow.copy(amount = it.replace(',', '.').toDouble()))
                } catch (_: Exception) {
                }
            },
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp).weight(1f),
            label = { Text("Amount") },
            trailingIcon = { Text("₽") },
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            maxLines = 1,
        )

        val datePicker = rememberDatePicker()
        Text(
            LocalDate.ofEpochDay(cashFlow.dateEpochDay).toString(),
            Modifier.padding(horizontal = 6.dp).clickable {
                datePicker.datePick(
                    {},
                    { onCashFlowChange(cashFlow.copy(dateEpochDay = it.toEpochDay())) },
                    LocalDate.ofEpochDay(cashFlow.dateEpochDay)
                )
            })

        if (interactable)
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Clear, contentDescription = null)
            }
    }
}