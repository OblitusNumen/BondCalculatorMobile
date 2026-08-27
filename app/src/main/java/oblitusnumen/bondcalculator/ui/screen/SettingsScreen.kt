package oblitusnumen.bondcalculator.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import oblitusnumen.bondcalculator.impl.getSettings
import oblitusnumen.bondcalculator.impl.putSettings
import oblitusnumen.bondcalculator.ui.BackPressButton
import oblitusnumen.bondcalculator.ui.addSetting
import oblitusnumen.bondcalculator.ui.cursorToEnd

@Composable
fun SettingsScreen(backPress: () -> Unit) {
    val context = LocalContext.current

    var settings by remember { mutableStateOf(getSettings(context)) }
    LaunchedEffect(settings) {
        putSettings(context, settings)
    }

    BackHandler(onBack = backPress)

    Scaffold(topBar = { SettingsTopBar(backPress) }) { paddingValues ->
        var taxPercentageText: TextFieldValue by remember { mutableStateOf(TextFieldValue(settings.taxPercentage.toString()).cursorToEnd()) }

        var brokerCommissionPercentageText: TextFieldValue by remember { mutableStateOf(TextFieldValue(settings.brokerCommissionPercentage.toString()).cursorToEnd()) }
        val brokerCommissionPercentageFocusRequester = remember { FocusRequester() }

        var marketCommissionPercentageText: TextFieldValue by remember { mutableStateOf(TextFieldValue(settings.marketCommissionPercentage.toString()).cursorToEnd()) }
        val marketCommissionPercentageFocusRequester = remember { FocusRequester() }

        var bondReinvestThruDepositPercentageText: TextFieldValue by remember { mutableStateOf(TextFieldValue(settings.bondReinvestThruDepositPercentage.toString()).cursorToEnd()) }
        val bondReinvestThruDepositPercentageFocusRequester = remember { FocusRequester() }

        var daysInYearText: TextFieldValue by remember { mutableStateOf(TextFieldValue(settings.daysInYear.toString()).cursorToEnd()) }
        val daysInYearFocusRequester = remember { FocusRequester() }

        LazyColumn(contentPadding = paddingValues) {
            addSetting(
                "Tax", taxPercentageText,
                {
                    try {
                        if (it.text.isNotEmpty())
                            settings = settings.copy(taxPercentage = it.text.toDouble())
                        taxPercentageText = it
                    } catch (_: Exception) {
                    }
                },
                KeyboardType.Decimal,
                focusRequester = null,
                nextFocusRequester = brokerCommissionPercentageFocusRequester
            ) {
                Text("%")
            }

            addSetting(
                "Broker commission", brokerCommissionPercentageText,
                {
                    try {
                        if (it.text.isNotEmpty())
                            settings = settings.copy(brokerCommissionPercentage = it.text.toDouble())
                        brokerCommissionPercentageText = it
                    } catch (_: Exception) {
                    }
                },
                KeyboardType.Decimal,
                focusRequester = brokerCommissionPercentageFocusRequester,
                nextFocusRequester = marketCommissionPercentageFocusRequester
            ) {
                Text("%")
            }

            addSetting(
                "Market commission", marketCommissionPercentageText,
                {
                    try {
                        if (it.text.isNotEmpty())
                            settings = settings.copy(marketCommissionPercentage = it.text.toDouble())
                        marketCommissionPercentageText = it
                    } catch (_: Exception) {
                    }
                },
                KeyboardType.Decimal,
                focusRequester = marketCommissionPercentageFocusRequester,
                nextFocusRequester = bondReinvestThruDepositPercentageFocusRequester
            ) {
                Text("%")
            }

            addSetting(
                "Bond reinvest rate via deposit", bondReinvestThruDepositPercentageText,
                {
                    try {
                        if (it.text.isNotEmpty())
                            settings = settings.copy(bondReinvestThruDepositPercentage = it.text.toDouble())
                        bondReinvestThruDepositPercentageText = it
                    } catch (_: Exception) {
                    }
                },
                KeyboardType.Decimal,
                focusRequester = bondReinvestThruDepositPercentageFocusRequester,
                nextFocusRequester = daysInYearFocusRequester
            ) {
                Text("%")
            }

            addSetting(
                "Days in year", daysInYearText,
                {
                    try {
                        if (it.text.isNotEmpty())
                            settings = settings.copy(daysInYear = it.text.toInt())
                        daysInYearText = it
                    } catch (_: Exception) {
                    }
                },
                KeyboardType.Number,
                focusRequester = daysInYearFocusRequester
            ) {
                Text("d")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(backPress: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = { BackPressButton(backPress) },
        title = { Text("Settings", maxLines = 1) },
    )
}
