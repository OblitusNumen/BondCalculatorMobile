package oblitusnumen.bondcalculator.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "currencies"
)
data class CurrencyRateEntity(
    @PrimaryKey
    val id: String,

    val charCode: String,
    val numCode: String,
    val nominal: Int,
    val name: String,
    val value: Double,
    val vUnitRate: Double,
    val date: LocalDate,

    val cacheDate: LocalDate
)