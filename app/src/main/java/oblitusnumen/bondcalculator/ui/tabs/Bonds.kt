package oblitusnumen.bondcalculator.ui.tabs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import oblitusnumen.bondcalculator.data.network.RemoteDataStatus
import oblitusnumen.bondcalculator.data.schema.BondDetails
import oblitusnumen.bondcalculator.data.schema.LocalBond
import oblitusnumen.bondcalculator.impl.*
import oblitusnumen.bondcalculator.ui.DatePicker
import oblitusnumen.bondcalculator.ui.composition.LocalDataManager
import oblitusnumen.bondcalculator.ui.cursorToEnd
import oblitusnumen.bondcalculator.ui.elements.Bond
import oblitusnumen.bondcalculator.ui.elements.bond
import oblitusnumen.bondcalculator.ui.screen.MainScreenSettings
import oblitusnumen.bondcalculator.ui.toLastUpdateString
import java.time.LocalDate
import java.time.LocalDateTime

// FIXME: draw favourites on top
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
    var favouriteBonds by remember { mutableStateOf(getBondFavourites(context)) }
    var updater by rememberSaveable { mutableStateOf(true) }
    var bonds by remember(updater) {
        mutableStateOf(getBonds(context).sortedWith(compareBy<LocalBond> { it.maturityDate }.thenBy { it.name }
            .thenBy { it.id }))
    }

    var remoteBonds: List<Pair<String, String?>>? by remember(search) { mutableStateOf(null) }
    var remoteDataStatus by remember(search) { mutableStateOf(RemoteDataStatus.Loading) }
    val coroutineScope = rememberCoroutineScope()

    val dataManager = LocalDataManager.current
    val displayBond: @Composable (String, String?, BondDetails?, LocalDateTime?, (BondDetails) -> Unit) -> Unit =
        { secId, shortName, bond, updateTime, onUpdateBond ->
            var updater by remember { mutableStateOf(false) }
            if (updateTime != null) {
                Row(
                    Modifier.border(
                        1.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                        RoundedCornerShape(4.dp)
                    ).padding(4.dp).fillMaxWidth(),
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
                        {
                            dataManager.bondRepository.refreshBond(secId)
                            updater = !updater
                        },
                        Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                    }
                }
            }
            if (bond == null) {
                Column {
                    Text(
                        "Bond $shortName seems to be absent",
                        Modifier.padding(16.dp).fillMaxWidth()
                            .align(CenterHorizontally),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                var calculateResult: BondDetails.CalculateResult? by remember { mutableStateOf(null) }
                var calculationStatus by remember { mutableStateOf(RemoteDataStatus.Loading) }

                LaunchedEffect(
                    bond,
                    settings,
                    rememberedMainScreenSettings.bondsCommission,
                    rememberedMainScreenSettings.bondsTaxed,
                    LocalDate.ofEpochDay(rememberedMainScreenSettings.bondsInvestmentDate),
                    rememberedMainScreenSettings.bondNumberOfLots,
                    updater
                ) {
                    calculationStatus = RemoteDataStatus.Loading
                    val bondization = dataManager.bondizationRepository.getBondization(secId)
                    if (bondization == null || bondization.isEmpty()) {
                        println("Calculation $secId fail")
                        calculationStatus = RemoteDataStatus.Failed
                    } else {
                        println("Calculation $secId")
                        calculationStatus = RemoteDataStatus.Loaded
                        calculateResult = bond.calculateProfit(
                            bondization,
                            settings,
                            rememberedMainScreenSettings.bondsCommission,
                            rememberedMainScreenSettings.bondsTaxed,
                            LocalDate.ofEpochDay(rememberedMainScreenSettings.bondsInvestmentDate),
                            rememberedMainScreenSettings.bondNumberOfLots,
                        )
                        println("Calculation result $secId: $calculateResult")
                    }
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
                                saveBond(context, bond.toLocalBond(id))
                                updater = !updater
                                saveBondShown = false
                            }) { Text("Save") }
                        }, title = { Text("Save bond $shortName") })
                }

                Bond(bond, calculateResult, favouriteBonds.contains(secId), {
                    if (it) {
                        favouriteBonds += secId
                    } else {
                        favouriteBonds -= secId
                    }
                    setBondFavourites(context, favouriteBonds)
                }, { saveBondShown = true }, onUpdateBond)
            }
        }

    val searchedBonds by remember(bonds, search) {
        mutableStateOf(bonds.filter { it.name.contains(search, true) }
            .sortedWith(compareBy<LocalBond> { it.name.indexOf(search) }.thenBy { it.maturityDate }.thenBy { it.name }
                .thenBy { it.id }))
    }

    var prevSearch by rememberSaveable { mutableStateOf(search) }

    val appDataManager = LocalDataManager.current
    LaunchedEffect(remoteDataStatus, search) {
        if (search.isNotEmpty()/* && prevSearch != search*/) {
            if (remoteDataStatus == RemoteDataStatus.Loading) {
                appDataManager.bondRepository.searchBonds(search) { status, bonds ->
                    remoteBonds = bonds
                    remoteDataStatus = status
                }
//            fetchRemote(search)
            }
        }
    }

    LaunchedEffect(search) {
        if (search.isNotEmpty() && prevSearch != search) {
            prevSearch = search
            remoteDataStatus = RemoteDataStatus.Loading
            coroutineScope.launch {
                rememberedLazyListState.animateScrollToItem(index = 0)
            }
        }
    }

    Column {
        LazyColumn(Modifier.fillMaxWidth().weight(1f), state = rememberedLazyListState) {
            if (bonds.isEmpty() && favouriteBonds.isEmpty() && search.isEmpty()) {
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

                    when (remoteDataStatus) {
                        RemoteDataStatus.Loaded, RemoteDataStatus.Cached -> {
                            if (remoteDataStatus == RemoteDataStatus.Cached) {
                                item {
                                    Text(
                                        "Offline search",
                                        Modifier.padding(16.dp).fillMaxWidth().align(CenterHorizontally),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
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
                                    item(key = "market:$secId") {
                                        var bond: BondDetails? by remember { mutableStateOf(null) }
                                        var updateTime: LocalDateTime? by remember { mutableStateOf(null) }

                                        DisposableEffect(secId) {
                                            val callback: (BondDetails?, LocalDateTime?) -> Unit =
                                                { bondDetails, lastUpdate ->
                                                    bond = bondDetails
                                                    updateTime = lastUpdate
                                                }
                                            dataManager.bondRepository.getBondSubscribe(secId, callback)
                                            println("Sub $secId")
                                            onDispose {
                                                dataManager.bondRepository.getBondUnsubscribe(secId, callback)
                                                println("Unsub $secId")
                                            }
                                        }

                                        if (updateTime == null) {
                                            Text(
                                                "Loading...",
                                                Modifier.height(250.dp).fillMaxWidth().align(CenterHorizontally),
                                                textAlign = TextAlign.Center
                                            )
                                        } else {
                                            displayBond(secId, shortName, bond, updateTime) {
                                                bond = it
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        RemoteDataStatus.Loading -> {
                            item {
                                Text(
                                    "Loading...",
                                    Modifier.padding(16.dp).fillMaxWidth().align(CenterHorizontally),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        RemoteDataStatus.Failed -> {
                            item {
                                Text(
                                    "Failed to fetch bonds from market",
                                    Modifier.padding(16.dp).fillMaxWidth().align(CenterHorizontally),
                                    textAlign = TextAlign.Center
                                )
                                IconButton(
                                    {
                                        remoteDataStatus = RemoteDataStatus.Loading
//                                        fetchRemote(search)
                                    },
                                    Modifier.padding(16.dp).padding(top = 0.dp).fillMaxWidth()
                                        .align(CenterHorizontally)
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

                for (secId in (favouriteBonds - (remoteBonds?.map { it.first }?.toSet() ?: emptySet())).sorted()) {
                    item(key = "favourite:$secId") {
                        var bond: BondDetails? by remember { mutableStateOf(null) }
                        var updateTime: LocalDateTime? by remember { mutableStateOf(null) }

                        DisposableEffect(secId) {
                            val callback: (BondDetails?, LocalDateTime?) -> Unit =
                                { bondDetails, lastUpdate ->
                                    bond = bondDetails
                                    updateTime = lastUpdate
                                }
                            dataManager.bondRepository.getBondSubscribe(secId, callback)
                            println("Sub $secId")
                            onDispose {
                                dataManager.bondRepository.getBondUnsubscribe(secId, callback)
                                println("Unsub $secId")
                            }
                        }

                        if (updateTime == null) {
                            Text(
                                "Loading...",
                                Modifier.height(250.dp).fillMaxWidth().align(CenterHorizontally),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            displayBond(secId, secId, bond, updateTime) {
                                bond = it
                            }
                        }
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

