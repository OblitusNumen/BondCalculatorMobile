package oblitusnumen.bondcalculator.ui.test

import androidx.compose.runtime.staticCompositionLocalOf
import oblitusnumen.bondcalculator.ui.test.ActivityContainer

val LocalActivityContainer =
    staticCompositionLocalOf<ActivityContainer> {
        error("ActivityContainer is not provided")
    }