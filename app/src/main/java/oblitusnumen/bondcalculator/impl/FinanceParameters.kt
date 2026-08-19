package oblitusnumen.bondcalculator.impl

import androidx.compose.runtime.saveable.mapSaver
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class FinanceParameters(
    val taxPercentage: Double = 13.0,
    val brokerCommissionPercentage: Double = .06,
    val marketCommissionPercentage: Double = .016,
    val bondReinvestThruDepositPercentage: Double = 10.0,
    val daysInYear: Int = 365,
) {
    val commissionRate: Double
        get() = (brokerCommissionPercentage + marketCommissionPercentage) * .01

    override fun toString(): String = Json.encodeToString(serializer(), this)

    companion object {
        val saver = mapSaver(
            save = { mapOf("it" to it.toString()) },
            restore = { fromString(it["it"] as String) }
        )

        fun fromString(string: String?): FinanceParameters {
            return if (string.isNullOrEmpty())
                FinanceParameters(13.0, .06, .016, 10.0, 365)
            else
                Json.decodeFromString(serializer(), string)
        }
    }
}