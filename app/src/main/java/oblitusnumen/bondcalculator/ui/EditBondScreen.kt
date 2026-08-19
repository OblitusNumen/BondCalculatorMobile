package oblitusnumen.bondcalculator.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import oblitusnumen.bondcalculator.impl.*
import java.time.LocalDate

@Composable
fun EditBondScreen(backPress: () -> Unit, bondId: Int? = null) {
    val context = LocalContext.current
    val datePicker = remember { DatePicker() }
    datePicker.TryCompose()

    var bond by remember { mutableStateOf(getBond(context, bondId) ?: Bond(getBondId(context))) }
    var nameText: TextFieldValue by remember { mutableStateOf(TextFieldValue(bond.name).cursorToEnd()) }
    var bondValueText: TextFieldValue by remember { mutableStateOf(TextFieldValue(bond.bondValue.toString()).cursorToEnd()) }
    var bondReturnDateText: LocalDate by remember { mutableStateOf(bond.bondReturnDate) }
    var couponPeriodDaysText: TextFieldValue by remember { mutableStateOf(TextFieldValue(bond.couponPeriodDays.toString()).cursorToEnd()) }
    var couponValueText: TextFieldValue by remember { mutableStateOf(TextFieldValue(bond.couponValue.toString()).cursorToEnd()) }
    var bondPriceText: TextFieldValue by remember { mutableStateOf(TextFieldValue(bond.bondPrice.toString()).cursorToEnd()) }
    var nkdOffsetText: TextFieldValue by remember { mutableStateOf(TextFieldValue(bond.nkdOffset.toString()).cursorToEnd()) }
    val bondPriceFocusRequester = remember { FocusRequester() }
    val bondValueFocusRequester = remember { FocusRequester() }
    val couponPeriodDaysFocusRequester = remember { FocusRequester() }
    val selectBondReturnDate = {
        datePicker.datePick({ couponPeriodDaysFocusRequester.requestFocus() }, {
            bond = bond.withBondReturnDate(it)
            bondReturnDateText = it
            couponPeriodDaysFocusRequester.requestFocus()
        }, bondReturnDateText)
    }
    val couponValueFocusRequester = remember { FocusRequester() }
    val nkdOffsetFocusRequester = remember { FocusRequester() }

    BackHandler(onBack = backPress)

    Scaffold(topBar = {
        EditBondTopBar(backPress, bondId == null) {
            if (bondId == null)
                incBondId(context)
            saveBond(context, bond)
            backPress()
        }
    }) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            addSetting(
                "Name", nameText, {
                    nameText = it
                    bond = bond.copy(name = it.text)
                },
                KeyboardType.Text,
                focusRequester = null,
                nextFocusRequester = bondPriceFocusRequester
            ) {
            }

            addSetting(
                "Bond price", bondPriceText, {
                    try {
                        if (it.text.isNotEmpty())
                            bond = bond.copy(bondPrice = it.text.toDouble())
                        bondPriceText = it
                    } catch (_: Exception) {
                    }
                },
                keyboardType = KeyboardType.Decimal,
                focusRequester = bondPriceFocusRequester,
                nextFocusRequester = bondValueFocusRequester
            ) {
                Text("₽")
            }

            addSetting(
                "Bond value", bondValueText, {
                    try {
                        if (it.text.isNotEmpty())
                            bond = bond.copy(bondValue = it.text.toDouble())
                        bondValueText = it
                    } catch (_: Exception) {
                    }
                },
                keyboardType = KeyboardType.Decimal,
                focusRequester = bondValueFocusRequester,
                onDone = { selectBondReturnDate() }
            ) {
                Text("₽")
            }

            item {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Return date: ", Modifier.padding(start = 8.dp), fontSize = 24.sp)

                    TextButton(onClick = selectBondReturnDate, Modifier.padding(horizontal = 8.dp)) {
                        Text(bondReturnDateText.toString(), fontSize = 24.sp)
                    }
                }
            }

            addSetting(
                "Coupon period", couponPeriodDaysText,
                {
                    try {
                        if (it.text.isNotEmpty())
                            bond = bond.copy(couponPeriodDays = it.text.toInt())
                        couponPeriodDaysText = it
                    } catch (_: Exception) {
                    }
                },
                KeyboardType.Number,
                focusRequester = couponPeriodDaysFocusRequester,
                nextFocusRequester = couponValueFocusRequester,
            ) {
                Text("d")
            }

            addSetting(
                "Coupon value", couponValueText, {
                    try {
                        if (it.text.isNotEmpty())
                            bond = bond.copy(couponValue = it.text.toDouble())
                        couponValueText = it
                    } catch (_: Exception) {
                    }
                },
                keyboardType = KeyboardType.Decimal,
                focusRequester = couponValueFocusRequester,
                nextFocusRequester = nkdOffsetFocusRequester
            ) {
                Text("₽")
            }

            addSetting(
                "НКД offset", nkdOffsetText,
                {
                    try {
                        if (it.text.isNotEmpty() && it.text != "-")
                            bond = bond.copy(nkdOffset = it.text.toInt())
                        nkdOffsetText = it
                    } catch (_: Exception) {
                    }
                },
                KeyboardType.Number,
                focusRequester = nkdOffsetFocusRequester,
                onDone = { defaultKeyboardAction(ImeAction.Done) }
            ) {
                Text("d")
            }

            item {
                Box(Modifier.fillMaxWidth().padding(top = 64.dp, bottom = 16.dp), contentAlignment = Center) {
                    Button(
                        onClick = {
                            deleteBond(context, bond)
                            backPress()
                        }, Modifier.padding(horizontal = 8.dp).fillMaxWidth(), colors = ButtonColors(
                            containerColor = Color.Red,
                            contentColor = Color.DarkGray,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = Color.Transparent,
                        )
                    ) { Text("Delete") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBondTopBar(backPress: () -> Unit, new: Boolean, onSave: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = { BackPressButton(backPress) },
        title = { Text(if (new) "Create bond" else "Edit bond", maxLines = 1) },
        actions = { TextButton(onSave) { Text("Save") } },
    )
}