package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.Serializable

@Serializable
data class BondPointer(val secid: String, val shortName: String?)