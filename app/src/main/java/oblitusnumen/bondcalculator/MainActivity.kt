package oblitusnumen.bondcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import oblitusnumen.bondcalculator.data.AppDataManager
import oblitusnumen.bondcalculator.ui.composition.LocalDataManager
import oblitusnumen.bondcalculator.ui.screen.*
import oblitusnumen.bondcalculator.ui.test.ActivityContainer
import oblitusnumen.bondcalculator.ui.test.LocalActivityContainer
import oblitusnumen.bondcalculator.ui.theme.BondCalculatorMobileTheme

class MainActivity : ComponentActivity() {
    lateinit var dataManager: AppDataManager
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = ActivityContainer(this)
        dataManager = AppDataManager(this)

        enableEdgeToEdge()
        setContent {
            val coroutineScope = rememberCoroutineScope()
            remember {
                dataManager.setCoroutineScope(coroutineScope)
            }
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    container.fetch()
                }
            }

            BondCalculatorMobileTheme {
                CompositionLocalProvider(
                    LocalActivityContainer provides container,
                    LocalDataManager provides dataManager
                ) {
                    var openScreen by rememberSaveable { mutableStateOf(OpenScreen.Main) }
                    var editBondId: Int? by rememberSaveable { mutableStateOf(null) }
                    val mainScreenSettings = rememberMainScreenSettings()
                    val rememberedSearch = rememberSaveable { mutableStateOf("") }
                    val rememberedPagerState = rememberPagerState(Tab.Bonds.ordinal, pageCount = { Tab.entries.size })
                    val lazyListState = rememberLazyListState()

                    when (openScreen) {
                        OpenScreen.Main -> {
                            MainScreen({ openScreen = OpenScreen.Settings }, {
                                if (it == null) {
                                    openScreen = OpenScreen.CreateBond
                                } else {
                                    editBondId = it
                                    openScreen = OpenScreen.EditBond
                                }
                            }, mainScreenSettings, rememberedPagerState, rememberedSearch, lazyListState)
                        }

                        OpenScreen.Settings -> {
                            SettingsScreen { openScreen = OpenScreen.Main }
                        }

                        OpenScreen.EditBond ->
                            EditBondScreen(
                                {
                                    editBondId = null
                                    openScreen = OpenScreen.Main
                                }, editBondId!!
                            )

                        OpenScreen.CreateBond -> EditBondScreen({ openScreen = OpenScreen.Main })
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        dataManager.close()
        super.onDestroy()
    }
}

@Serializable
enum class OpenScreen {
    Main,
    Settings,
    EditBond,
    CreateBond,
}