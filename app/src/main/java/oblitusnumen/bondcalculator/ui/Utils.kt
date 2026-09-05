package oblitusnumen.bondcalculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

fun LocalDateTime?.toLastUpdateString(): String {
    return "UPD: ${this?.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))}"
}

fun LocalDateTime.toLastUpdateString(other: LocalDateTime): String {
    val duration = Duration.between(other, this)
    val days = duration.toDays()
    val hours = duration.toHours()
    val minutes = duration.toMinutes()
    duration.seconds

    return if (days.toInt() == 0)
        if (hours.toInt() == 0)
            if (minutes.toInt() == 0)
                "just now"
            else
                "${minutes}m ago"
        else
            "${hours}h ago"
    else
        "outdated"

//    return if (days.toInt() == 0)
//        if (hours.toInt() == 0)
//            if (minutes.toInt() == 0)
//                "${seconds}s ago"
//            else
//                "${minutes}min ago"
//        else
//            "${hours}h ago"
//    else if (days < 30)
//        "${days}d ago"
//    else
//        "a long time ago"
}

fun Double.toSigFigString(sigFigs: Int = 3): String {
    return BigDecimal(this.toString())
        .round(MathContext(sigFigs, RoundingMode.HALF_UP))
        .stripTrailingZeros()
        .toPlainString()
}

fun formatDouble(value: Double, digits: Int = 2): String = String.format("%.${digits}f", value)

fun formatDoublePercentage(value: Double, digits: Int = 2): String {
    val value =
        if (value.absoluteValue > 1000000)
            if (value < 0)
                Double.NEGATIVE_INFINITY
            else
                Double.POSITIVE_INFINITY
        else
            value
    return "${formatDouble(value, digits)}%"
}

fun formatRubbleValue(value: Double, digits: Int = 2): String = "${formatDouble(value, digits)}₽"

fun formatPeriod(period: Int): String {
    val years = period / 365
    val months = (period % 365) / 30
    val days = period % 365 % 30
    return (if (years > 0) "${years}y" else "") +
            (if (years > 0 && months > 0) " " else "") +
            (if (months == 0) "" else "${months}m") +
            (if (months == 0 && years == 0) "${days}d" else "")
}

@Composable
fun BackPressButton(backPress: () -> Unit) {
    IconButton(onClick = backPress) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null
        )
    }
}

@Composable
fun ParameterRow(parameterName: String, parameterValue: String, modifier: Modifier, level: Int = 0) {
    Row(
        verticalAlignment = CenterVertically,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            parameterName,
            Modifier.padding(2.dp).padding(start = (6 * level + 2).dp),
            fontSize = 10.sp,
            textAlign = TextAlign.Start,
            fontFamily = Monospace,
        )
        Text(
            parameterValue,
            Modifier.padding(2.dp),
            fontSize = 10.sp,
            textAlign = TextAlign.End,
            fontFamily = Monospace,
        )
    }
}

fun LazyListScope.addSetting(
    settingName: String,
    settingValue: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    keyboardType: KeyboardType,
    focusRequester: FocusRequester?,
    nextFocusRequester: FocusRequester?,
    isError: Boolean = false,
    trailingIcon: @Composable () -> Unit = {},
) {
    addSetting(
        settingName,
        settingValue,
        onChange,
        keyboardType,
        isError,
        focusRequester,
        { nextFocusRequester?.requestFocus() },
        trailingIcon
    )
}

fun LazyListScope.addSetting(
    settingName: String,
    settingValue: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    keyboardType: KeyboardType,
    isError: Boolean = false,
    focusRequester: FocusRequester? = null,
    onDone: KeyboardActionScope.() -> Unit = { this.defaultKeyboardAction(ImeAction.Done) },
    trailingIcon: @Composable () -> Unit = {},
) {
    item {
        OutlinedTextField(
            value = settingValue,
            onValueChange = onChange,
            isError = isError,
            modifier = (if (focusRequester == null) Modifier else Modifier.focusRequester(focusRequester)).padding(
                horizontal = 12.dp,
                vertical = 4.dp
            ).fillMaxWidth(),
            label = @Composable { Text(settingName) },
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = onDone),
            maxLines = 1,
        )
    }
}

fun TextFieldValue.cursorToEnd(): TextFieldValue = this.copy(selection = TextRange(this.text.length))

fun Modifier.selectAllOnFocus(
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit
): Modifier = this.onFocusChanged { focusState ->
    if (focusState.hasFocus) {
        onTextFieldValueChange(textFieldValue.selectAll())
    }
}

fun TextFieldValue.selectAll(): TextFieldValue = this.copy(selection = TextRange(0, this.text.length))

fun LazyListScope.addSetting(
    settingName: String,
    settingControl: @Composable () -> Unit,
) {
    item {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = CenterVertically
        ) {
            Text(
                settingName,
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            settingControl()
        }
    }
}