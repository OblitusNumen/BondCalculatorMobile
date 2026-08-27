package oblitusnumen.bondcalculator.ui.composition

import androidx.compose.runtime.staticCompositionLocalOf
import oblitusnumen.bondcalculator.data.AppDataManager

val LocalDataManager =
    staticCompositionLocalOf<AppDataManager> {
        error("AppDataManager was not provided")
    }