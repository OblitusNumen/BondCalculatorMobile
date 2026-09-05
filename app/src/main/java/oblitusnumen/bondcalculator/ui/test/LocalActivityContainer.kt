package oblitusnumen.bondcalculator.ui.test

import androidx.compose.runtime.staticCompositionLocalOf

val LocalActivityContainer =
    staticCompositionLocalOf<ActivityContainer> {
        error("ActivityContainer is not provided")
    }