package oblitusnumen.bondcalculator.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch
import oblitusnumen.bondcalculator.impl.*
import oblitusnumen.bondcalculator.impl.moexapi.MoexApiClient
import oblitusnumen.bondcalculator.ui.*
import java.time.LocalDate


@Composable
fun BondsTab(
    paddingValues: PaddingValues,
    search: String,
    rememberedLazyListState: LazyListState,
    rememberedMainScreenSettings: MainScreenSettings,
    openEditBond: (Int?) -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(getSettings(context)) }
    var updater by rememberSaveable { mutableStateOf(true) }
    var bonds by remember(updater) {
        mutableStateOf(getBonds(context).sortedWith(compareBy<Bond> { it.bondReturnDate }.thenBy { it.name }
            .thenBy { it.id }))
    }

    var remoteBonds: List<Pair<String, String?>>? by remember(search) { mutableStateOf(null) }
    var remoteBondsState by remember(search) { mutableStateOf(RemoteBondsState.Loading) }
    val coroutineScope = rememberCoroutineScope()
    val fetchRemote: (query: String) -> Unit = { query ->
        coroutineScope.launch {
            val service = MoexApiClient()

            try {
                remoteBonds = service.searchSecurities(query).filter { it.group == "stock_bonds" }
                    .filter {
                        it.shortname?.contains(query, true) ?: false ||
                                it.name?.contains(query, true) ?: false ||
                                it.isin?.contains(query, true) ?: false
                    }
                    .map { it.secid to it.shortname }
                remoteBondsState = RemoteBondsState.Loaded
            } catch (e: Exception) {
                remoteBondsState = RemoteBondsState.Failed
                e.printStackTrace()
            } finally {
                service.close()
            }
        }
    }

    val searchedBonds by remember(bonds, search) {
        mutableStateOf(bonds.filter { it.name.contains(search, true) }
            .sortedWith(compareBy<Bond> { it.name.indexOf(search) }.thenBy { it.bondReturnDate }.thenBy { it.name }
                .thenBy { it.id }))
    }

    var prevSearch by rememberSaveable { mutableStateOf(search) }

    LaunchedEffect(remoteBondsState, search) {
        if (remoteBondsState == RemoteBondsState.Loading) {
            fetchRemote(search)
        }
    }

    LaunchedEffect(search) {
        if (search.isNotEmpty() && prevSearch != search) {
            prevSearch = search
            coroutineScope.launch {
                rememberedLazyListState.animateScrollToItem(index = 0)
            }
        }
    }

    Column {
        LazyColumn(Modifier.fillMaxWidth().weight(1f), state = rememberedLazyListState) {
            if (bonds.isEmpty() && search.isEmpty()) {
                item {
                    Text(
                        "No bonds found",
                        Modifier.padding(16.dp).fillMaxWidth().align(CenterHorizontally),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                searchedBonds.forEach { bond ->
                    bond(
                        bond.id,
                        bond,
                        settings,
                        rememberedMainScreenSettings.bondsCommission,
                        rememberedMainScreenSettings.bondsTaxed,
                        LocalDate.ofEpochDay(rememberedMainScreenSettings.bondsInvestmentDate),
                        rememberedMainScreenSettings.bondNumberOfLots,
                        bonds,
                        openEditBond
                    ) { bond ->
                        saveBond(context, bond)
                        updater = !updater
                    }
                }

                if (search.isNotEmpty()) {
                    item {
                        Text(
                            "Bonds from market",
                            Modifier.padding(16.dp).fillMaxWidth().align(CenterHorizontally),
                            textAlign = TextAlign.Center
                        )
                    }

                    when (remoteBondsState) {
                        RemoteBondsState.Loaded -> {
                            if (remoteBonds?.isEmpty() ?: true) {
                                item {
                                    Text(
                                        "Nothing found on market",
                                        Modifier.padding(16.dp).fillMaxWidth().align(CenterHorizontally),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                for ((secId, shortName) in remoteBonds!!) {
                                    item(key = secId) {
                                        var bond: Bond? by remember { mutableStateOf(null) }
                                        var dataState: RemoteBondsState by remember { mutableStateOf(RemoteBondsState.Loading) }
                                        val fetchBond: () -> Unit = {
                                            coroutineScope.launch {
                                                val service = MoexApiClient()

                                                try {
                                                    bond = service.fetchBond(secId)?.toLocalBond(0)
                                                    dataState = RemoteBondsState.Loaded
                                                } catch (e: Exception) {
                                                    dataState = RemoteBondsState.Failed
                                                    e.printStackTrace()
                                                } finally {
                                                    service.close()
                                                }
                                            }
                                        }

                                        when (dataState) {
                                            RemoteBondsState.Loaded -> {
                                                if (bond == null) {
                                                    Text(
                                                        "Bond $shortName seems to be absent",
                                                        Modifier.padding(16.dp).fillMaxWidth()
                                                            .align(CenterHorizontally),
                                                        textAlign = TextAlign.Center
                                                    )
                                                } else {
                                                    val calculateResult by remember(
                                                        bond, settings, rememberedMainScreenSettings.bondsCommission,
                                                        rememberedMainScreenSettings.bondsTaxed,
                                                        LocalDate.ofEpochDay(rememberedMainScreenSettings.bondsInvestmentDate),
                                                        rememberedMainScreenSettings.bondNumberOfLots
                                                    ) {
                                                        mutableStateOf(
                                                            bond!!.calculateProfit(
                                                                settings,
                                                                rememberedMainScreenSettings.bondsCommission,
                                                                rememberedMainScreenSettings.bondsTaxed,
                                                                LocalDate.ofEpochDay(rememberedMainScreenSettings.bondsInvestmentDate),
                                                                rememberedMainScreenSettings.bondNumberOfLots,
                                                            )
                                                        )
                                                    }

                                                    var saveBondShown by remember { mutableStateOf(false) }
                                                    if (saveBondShown) {
                                                        AlertDialog(
                                                            onDismissRequest = { saveBondShown = false },
                                                            dismissButton = {
                                                                TextButton({
                                                                    saveBondShown = false
                                                                }) { Text("Cancel") }
                                                            }, confirmButton = {
                                                                TextButton({
                                                                    val id = getBondId(context)
                                                                    incBondId(context)
                                                                    saveBond(context, bond!!.copy(id = id))
                                                                    updater = !updater
                                                                    saveBondShown = false
                                                                }) { Text("Save") }
                                                            }, title = { Text("Save bond $shortName") })
                                                    }

                                                    Bond(bond!!, calculateResult, { saveBondShown = true }) {
                                                        bond = it
                                                    }
                                                }
                                            }

                                            RemoteBondsState.Loading -> {
                                                LaunchedEffect(Unit) {
                                                    fetchBond()
                                                }
                                                Text(
                                                    "Loading...",
                                                    Modifier.padding(16.dp).fillMaxWidth().align(CenterHorizontally),
                                                    textAlign = TextAlign.Center
                                                )
                                            }

                                            RemoteBondsState.Failed -> {
                                                Text(
                                                    "Failed to load bond $shortName",
                                                    Modifier.padding(16.dp).fillMaxWidth().align(CenterHorizontally),
                                                    textAlign = TextAlign.Center
                                                )
                                                IconButton(
                                                    {
                                                        remoteBondsState = RemoteBondsState.Loading
                                                        fetchRemote(search)
                                                    },
                                                    Modifier.padding(16.dp).padding(top = 0.dp).fillMaxWidth()
                                                        .align(CenterHorizontally)
                                                ) {
                                                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        RemoteBondsState.Loading -> {
                            item {
                                Text(
                                    "Loading...",
                                    Modifier.padding(16.dp).fillMaxWidth().align(CenterHorizontally),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        RemoteBondsState.Failed -> {
                            item {
                                Text(
                                    "Failed to fetch bonds from market",
                                    Modifier.padding(16.dp).fillMaxWidth().align(CenterHorizontally),
                                    textAlign = TextAlign.Center
                                )
                                IconButton(
                                    {
                                        remoteBondsState = RemoteBondsState.Loading
                                        fetchRemote(search)
                                    },
                                    Modifier.padding(16.dp).padding(top = 0.dp).fillMaxWidth().align(CenterHorizontally)
                                ) {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Other bonds",
                            Modifier.padding(16.dp).fillMaxWidth().align(CenterHorizontally),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                (bonds - searchedBonds.toSet()).forEach { bond ->
                    bond(
                        bond.id,
                        bond,
                        settings,
                        rememberedMainScreenSettings.bondsCommission,
                        rememberedMainScreenSettings.bondsTaxed,
                        LocalDate.ofEpochDay(rememberedMainScreenSettings.bondsInvestmentDate),
                        rememberedMainScreenSettings.bondNumberOfLots,
                        bonds,
                        openEditBond
                    ) { bond ->
                        saveBond(context, bond)
                        updater = !updater
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = CenterHorizontally) {
                Switch(
                    rememberedMainScreenSettings.bondsTaxed,
                    onCheckedChange = { rememberedMainScreenSettings.bondsTaxed = it })
                Text("Taxed", fontSize = 8.sp)
            }
            Column(horizontalAlignment = CenterHorizontally) {
                Switch(
                    rememberedMainScreenSettings.bondsCommission,
                    onCheckedChange = { rememberedMainScreenSettings.bondsCommission = it })
                Text("Commission", fontSize = 8.sp)
            }
            Box(contentAlignment = Center) {
                val datePicker = remember { DatePicker() }
                datePicker.TryCompose()

                TextButton(onClick = {
                    datePicker.datePick({}, {
                        rememberedMainScreenSettings.bondsInvestmentDate = it.toEpochDay()
                    }, LocalDate.ofEpochDay(rememberedMainScreenSettings.bondsInvestmentDate))
                }, Modifier.padding(horizontal = 8.dp)) {
                    Column(horizontalAlignment = CenterHorizontally) {
                        Text(
                            LocalDate.ofEpochDay(rememberedMainScreenSettings.bondsInvestmentDate).toString(),
                            fontSize = 10.sp
                        )
                        Text("Investment date", fontSize = 8.sp)
                    }
                }
            }
            Column(horizontalAlignment = CenterHorizontally) {
                Row(verticalAlignment = CenterVertically) {
                    var bondNumberOfLotsText: TextFieldValue by remember {
                        mutableStateOf(
                            TextFieldValue(
                                rememberedMainScreenSettings.bondNumberOfLots.toString()
                            ).cursorToEnd()
                        )
                    }

                    IconButton(onClick = set@{
                        if (rememberedMainScreenSettings.bondNumberOfLots <= 1)
                            return@set
                        rememberedMainScreenSettings.bondNumberOfLots -= 1
                        bondNumberOfLotsText =
                            TextFieldValue(rememberedMainScreenSettings.bondNumberOfLots.toString()).cursorToEnd()
                    }, Modifier.padding(1.dp).size(48.dp)) {
                        Icon(Icons.AutoMirrored.Default.ArrowLeft, contentDescription = null)
                    }

                    Text(bondNumberOfLotsText.text)

                    IconButton(onClick = {
                        rememberedMainScreenSettings.bondNumberOfLots += 1
                        bondNumberOfLotsText =
                            TextFieldValue(rememberedMainScreenSettings.bondNumberOfLots.toString()).cursorToEnd()
                    }, Modifier.padding(1.dp).size(48.dp)) {
                        Icon(Icons.AutoMirrored.Default.ArrowRight, contentDescription = null)
                    }
                }
                Text("Lots", fontSize = 8.sp)
            }
        }

        Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
    }
}

fun LazyListScope.bond(
    key: Any,
    bond: Bond,
    settings: FinanceParameters,
    hasCommission: Boolean,
    isTaxed: Boolean,
    investmentDate: LocalDate,
    numberOfLots: Int,
    bonds: List<Bond>,
    openEditBond: (Int?) -> Unit,
    onBondSave: (Bond) -> Unit
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

        Bond(bond, calculateResult, openEditBond, onBondSave)
    }
}

@Composable
fun Bond(
    bond: Bond,
    calculateResult: Bond.CalculateResult,
    openEditBond: (Int?) -> Unit,
    onBondUpdate: (Bond) -> Unit
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

        //edit price
        var bondPriceText: TextFieldValue by remember { mutableStateOf(TextFieldValue(bond.bondPrice.toString()).cursorToEnd()) }
        OutlinedTextField(
            value = bondPriceText,
            onValueChange = {
                try {
                    if (it.text.isNotEmpty()) {
                        onBondUpdate(bond.copy(bondPrice = it.text.toDouble()))
                    }
                    bondPriceText = it
                } catch (_: Exception) {
                }
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp).fillMaxWidth(),
            label = @Composable { Text("Bond price") },
            trailingIcon = {
                Text("₽")
            },
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            maxLines = 1,
        )

        //result
        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                ParameterRow(
                    "НКД",
                    formatRubbleValue(calculateResult.nkd),
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Commission",
                    formatRubbleValue(calculateResult.buyCommission),
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Cost",
                    formatRubbleValue(calculateResult.investmentCost),
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Duration",
                    formatPeriod(calculateResult.investmentPeriod),
                    Modifier.padding(horizontal = 4.dp),
                )
            }
            Column(Modifier.weight(1f)) {
                ParameterRow(
                    "Clean profit",
                    formatDoublePercentage(calculateResult.cleanProfitRatePercentage),
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Clean profit",
                    formatRubbleValue(calculateResult.cleanProfit),
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Effective profit",
                    formatRubbleValue(calculateResult.effectiveProfit),
                    Modifier.padding(horizontal = 4.dp),
                )
                ParameterRow(
                    "Return date",
                    bond.bondReturnDate.toString(),
                    Modifier.padding(horizontal = 4.dp),
                )
            }
        }

        //coupons
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = CenterHorizontally) {
                Text("Coupon rate", fontSize = 8.sp)
                Text(formatDoublePercentage(calculateResult.nominalCouponRate))
            }
            Column(horizontalAlignment = CenterHorizontally) {
                Text("Total coupons", fontSize = 8.sp)
                Text(calculateResult.couponCount.toString())
            }
            var couponsShown by remember { mutableStateOf(false) }
            IconButton(onClick = { couponsShown = true }) {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (couponsShown) {
                AlertDialog(onDismissRequest = { couponsShown = false }, confirmButton = {
                    TextButton(onClick = { couponsShown = false }) {
                        Text("Ok")
                    }
                }, title = { Text("Coupons") }, text = {
                    LazyColumn(state = rememberLazyListState()) {
                        calculateResult.coupons.forEachIndexed { index, coupon ->
                            item {
                                Row(
                                    verticalAlignment = CenterVertically,
                                    modifier = (if (index % 2 == 0) Modifier.background(
                                        Color.Gray.copy(
                                            alpha = 0.1f
                                        )
                                    ) else Modifier).fillMaxWidth().padding(4.dp).padding(end = 12.dp)
                                ) {
                                    Text(
                                        coupon.date.toString(),
                                        Modifier.padding(2.dp).weight(1f),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        formatRubbleValue(coupon.value),
                                        Modifier.padding(2.dp).weight(1f),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                })
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun AllParametersDialog(
    bond: Bond,
    calculateResult: Bond.CalculateResult,
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
                        "Return date",
                        bond.bondReturnDate.toString(),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Investment span",
                        "${formatPeriod(calculateResult.investmentPeriod)}${if (calculateResult.investmentPeriod > 30) " / ${calculateResult.investmentPeriod}d" else ""}",
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Reinvest НКД",
                        formatRubbleValue(calculateResult.reinvestNkd),
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
                        "Bonds cost",
                        formatRubbleValue(calculateResult.bondsCost),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "НКД",
                        formatRubbleValue(calculateResult.nkd),
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
                    ParameterRow(
                        "Coupon amount",
                        formatRubbleValue(calculateResult.coupons.firstOrNull()?.value ?: 0.0),
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
                        "Reinvest НКД",
                        formatRubbleValue(calculateResult.reinvestNkd),
                        Modifier.padding(horizontal = padding)
                    )
                    ParameterRow(
                        "Next coupon",
                        calculateResult.coupons.firstOrNull()?.date?.toString() ?: "-",
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

@Composable
fun EfficiencyTable(calculateResult: Bond.CalculateResult) {
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
                    formatDoublePercentage(calculateResult.effectiveProfitRatePercentage),
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    formatRubbleValue(calculateResult.effectiveProfit),
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    formatRubbleValue(calculateResult.reinvestProfit),
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    formatDoublePercentage(calculateResult.effectiveProfitReinvestPercentage),
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    formatRubbleValue(calculateResult.effectiveReturn),
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
                    formatDoublePercentage(calculateResult.cleanProfitRatePercentage),
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    formatRubbleValue(calculateResult.cleanProfit),
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
                    formatRubbleValue(calculateResult.cleanReturn),
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
                    formatDoublePercentage(calculateResult.totalProfitRatePercentage),
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    formatRubbleValue(calculateResult.totalProfit),
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
                    formatRubbleValue(calculateResult.totalReturn),
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
                    formatDoublePercentage(calculateResult.depositEffectiveProfitRatePercentage),
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    formatRubbleValue(calculateResult.depositEffectiveProfit),
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    formatRubbleValue(calculateResult.depositReinvestProfit),
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    formatDoublePercentage(calculateResult.depositEffectiveProfitReinvestPercentage),
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
                Text(
                    formatRubbleValue(calculateResult.depositEffectiveReturn),
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp).fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    fontFamily = Monospace,
                )
            }
        }
    }
}

enum class RemoteBondsState {
    Loaded,
    Loading,
    Failed,
}