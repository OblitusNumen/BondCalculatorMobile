package oblitusnumen.bondcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import oblitusnumen.bondcalculator.ui.*
import oblitusnumen.bondcalculator.ui.theme.BondCalculatorMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BondCalculatorMobileTheme {
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

@Serializable
enum class OpenScreen {
    Main,
    Settings,
    EditBond,
    CreateBond,
}