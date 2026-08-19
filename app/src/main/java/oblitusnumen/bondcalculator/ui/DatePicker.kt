package oblitusnumen.bondcalculator.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun rememberDatePicker(): DatePicker {
    val datePicker = remember { DatePicker() }
    datePicker.TryCompose()
    return datePicker
}

@Composable
fun DatePicker(
    label: String,
    initialDate: LocalDate = LocalDate.now(),
    onDatePicked: (LocalDate) -> Unit,
) {
    // TODO:
}

class DatePicker {
    private var datePickerShown by mutableStateOf(false)
    private var selectedDate: LocalDate = LocalDate.now()
    private var initialDateTime: LocalDateTime = LocalDateTime.now()
    private var cancelCallback: (() -> Unit)? = null
    private var confirmCallback: ((LocalDateTime) -> Unit)? = null
    private var confirmCallbackDateOnly: ((LocalDate) -> Unit)? = null

    @Composable
    fun TryCompose() {
        if (datePickerShown) {
            DatePickerModal({
                if (confirmCallbackDateOnly != null) {
                    confirmCallbackDateOnly?.invoke(it)
                    cleanup()
                    return@DatePickerModal
                }
                selectedDate = it
                datePickerShown = false
            }, {
                cancelCallback?.invoke()
                cleanup()
            }, initialDateTime.toLocalDate())
        }
    }

    fun datePick(
        onCancel: () -> Unit,
        onConfirm: (LocalDate) -> Unit,
        initialDate: LocalDate = LocalDate.now()
    ) {
        if (datePickerShown)
            return
        cancelCallback = onCancel
        confirmCallbackDateOnly = onConfirm
        initialDateTime = initialDate.atStartOfDay()
        datePickerShown = true
    }

    private fun cleanup() {
        initialDateTime = LocalDateTime.now()
        cancelCallback = null
        confirmCallback = null
        confirmCallbackDateOnly = null
        datePickerShown = false
    }

    companion object {
        @Composable
        private fun DatePickerModal(
            onDateSelected: (LocalDate) -> Unit,
            onDismiss: () -> Unit,
            initialDate: LocalDate = LocalDate.now()
        ) {
            DatePickerModal(
                { onDateSelected(LocalDate.ofEpochDay(it / 86400000)) },
                onDismiss,
                initialDate.toEpochDay() * 86400000
            )
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        private fun DatePickerModal(
            onDateSelected: (Long) -> Unit,
            onDismiss: () -> Unit,
            initialDate: Long
        ) {
            val datePickerState =
                rememberDatePickerState(initialSelectedDateMillis = initialDate)

            DatePickerDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(onClick = {
                        val selMillis = datePickerState.selectedDateMillis
                        if (selMillis != null)
                            onDateSelected(selMillis)
                    }) {
                        Text("Ok")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> MaterialSpinner(
    title: String, options: List<T>,
    modifier: Modifier = Modifier,
    onSelect: (option: T) -> Unit, initialOption: T = options[0],
    labelFor: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(initialOption) }

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = it },
        modifier = Modifier.then(modifier)
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            value = labelFor(selectedOption),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(title, style = MaterialTheme.typography.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(labelFor(option), style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        selectedOption = option
                        onSelect(selectedOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
