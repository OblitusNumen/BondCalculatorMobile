package oblitusnumen.bondcalculator.ui.test

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import oblitusnumen.bondcalculator.data.schema.Bond
import kotlin.random.Random

@Composable
fun BondScreen(modifier: Modifier) {
    val container = LocalActivityContainer.current
    val rememberCoroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        rememberCoroutineScope.launch {
            container.fetch()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = "Облигации",
                style =
                    MaterialTheme.typography.headlineSmall
            )
            Button({
                rememberCoroutineScope.launch {
                    container.fetch()
                }
            }) {
                Text("Обновить")
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        val flow = flow {
            //rememberCoroutineScope.launch {
//                while (true) {
            val nextInt = Random.nextInt()
            println("$nextInt")
            emit("$nextInt")
//                    delay(100)
//                }
            //}
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                var a: String by remember { mutableStateOf("") }
                Text(a)

                LaunchedEffect(Unit) {
                    flow.collect {
                        a = it
                    }
                }
            }

            repeat(100) {
                item {
                    Text("Placeholder")
                }
            }

            if (container.bonds.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            "В базе данных нет облигаций"
                        )
                    }
                }
            } else {
                items(
                    items = container.bonds,
//                    key = { it.secid }
                ) { bond ->
//                    Text(bond)
                    BondRow(
                        bond = bond,
                        onClick = {
                            // FIXME:
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BondRow(bond: Bond, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() }) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = bond.secid, style = MaterialTheme.typography.titleMedium)
                Text(text = bond.shortname ?: "Без названия")
                Text(text = "ISIN: " + bond.isin)
                Text(text = "FaceUnit: " + bond.faceUnit)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "FaceValue: " + (bond.faceValue ?: "—"))
                Text(text = "Coupon: " + (bond.couponValue ?: "—"))
                Text(text = "CouponPeriod: " + (bond.couponPeriod ?: "—"))
                Text(text = "MaturityDate: " + (bond.matDate ?: "—"))
            }
        }
    }
}