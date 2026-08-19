package oblitusnumen.bondcalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import oblitusnumen.bondcalculator.ui.tabs.BondsTab
import oblitusnumen.bondcalculator.ui.tabs.DepositsTab
import oblitusnumen.bondcalculator.ui.tabs.ProfitIndexTab
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    openSettings: () -> Unit,
    openEditBond: (Int?) -> Unit,
    rememberedMainScreenSettings: MainScreenSettings,
    rememberedPagerState: PagerState,
    rememberedSearch: MutableState<String>,
    rememberedLazyListState: LazyListState,
) {
    var search by rememberSaveable { rememberedSearch }
    val coroutineScope = rememberCoroutineScope()

    Scaffold { paddingValues ->
        Column(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            MainTopBar(
                openSettings,
                { openEditBond(null) },
                search,
            ) {
                search = it
            }

            PrimaryScrollableTabRow(
                selectedTabIndex = rememberedPagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Tab.entries.forEachIndexed { index, destination ->
                    Tab(
                        selected = rememberedPagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                rememberedPagerState.scrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = when (destination) {
                                    Tab.Market -> "Market"
                                    Tab.Bonds -> "Bonds"
                                    Tab.Deposits -> "Deposits"
                                    Tab.ProfitIndex -> "Count profit"
                                },
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                            )
                        }
                    )
                }
            }

            HorizontalPager(rememberedPagerState, verticalAlignment = Alignment.Top) { page ->
                when (Tab.entries[page]) {
                    Tab.Market -> {
                        // TODO:
                    }

                    Tab.Bonds -> {
                        BondsTab(
                            paddingValues,
                            search,
                            rememberedLazyListState,
                            rememberedMainScreenSettings,
                            openEditBond
                        )
                    }

                    Tab.Deposits -> DepositsTab(paddingValues)

                    Tab.ProfitIndex -> ProfitIndexTab(paddingValues, rememberedMainScreenSettings)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(openSettings: () -> Unit, openCreateBond: () -> Unit, search: String, onSearch: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = CenterVertically) {
        IconButton(onClick = openSettings, Modifier.padding(4.dp)) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = null
            )
        }

        val focusRequester = remember { FocusRequester() }

        OutlinedTextField(
            value = search,
            onValueChange = onSearch,
            label = { Text("Search") },
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
            trailingIcon = {
                IconButton(onClick = {
                    if (search.isNotEmpty()) onSearch("")
                    else {
                        focusRequester.freeFocus()
                        focusRequester.requestFocus()
                    }}) {
                    Icon(
                        imageVector = if (search.isEmpty()) Icons.Filled.Search else Icons.Filled.Clear,
                        contentDescription = null,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            shape = RoundedCornerShape(8.dp),
            maxLines = 1,
        )

        IconButton(onClick = openCreateBond, Modifier.padding(4.dp)) {
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = CenterHorizontally) {
                Icon(Icons.Filled.Add, null)
//                Text("New bond", fontSize = 8.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun rememberMainScreenSettings(): MainScreenSettings =
    rememberSaveable(saver = MainScreenSettings.saver) { MainScreenSettings() }

class MainScreenSettings {
    var bondsTaxed: Boolean by mutableStateOf(true)
    var bondsCommission: Boolean by mutableStateOf(true)
    var bondsInvestmentDate: Long by mutableLongStateOf(LocalDate.now().toEpochDay())
    var bondNumberOfLots: Int by mutableIntStateOf(1)
    var profitTaxed: Boolean by mutableStateOf(true)
    var profitInvestmentCost: Double by mutableDoubleStateOf(1000.0)
    var profitTotalReturn: Double by mutableDoubleStateOf(1030.0)
    var profitInvestmentDate: Long by mutableLongStateOf(LocalDate.now().toEpochDay())
    var profitWithdrawDate: Long by mutableLongStateOf(LocalDate.now().plusDays(100).toEpochDay())

    companion object {
        val saver = mapSaver(
            save = {
                mapOf(
                    "bondsTaxed" to it.bondsTaxed,
                    "bondsCommission" to it.bondsCommission,
                    "bondsInvestmentDate" to it.bondsInvestmentDate,
                    "bondNumberOfLots" to it.bondNumberOfLots,
                    "profitTaxed" to it.profitTaxed,
                    "profitInvestmentCost" to it.profitInvestmentCost,
                    "profitTotalReturn" to it.profitTotalReturn,
                    "profitInvestmentDate" to it.profitInvestmentDate,
                    "profitWithdrawDate" to it.profitWithdrawDate,
                )
            },
            restore = {
                MainScreenSettings().apply {
                    bondsTaxed = it["bondsTaxed"] as Boolean
                    bondsCommission = it["bondsCommission"] as Boolean
                    bondsInvestmentDate = it["bondsInvestmentDate"] as Long
                    bondNumberOfLots = it["bondNumberOfLots"] as Int
                    profitTaxed = it["profitTaxed"] as Boolean
                    profitInvestmentCost = it["profitInvestmentCost"] as Double
                    profitTotalReturn = it["profitTotalReturn"] as Double
                    profitInvestmentDate = it["profitInvestmentDate"] as Long
                    profitWithdrawDate = it["profitWithdrawDate"] as Long
                }
            }
        )
    }
}

enum class Tab {
    Market,
    Bonds,
    Deposits,
    ProfitIndex
}
