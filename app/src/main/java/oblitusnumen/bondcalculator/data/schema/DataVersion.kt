package oblitusnumen.bondcalculator.data.schema

data class DataVersion(
    val dataVersion: Int,
    val seqnum: Long,
    val tradeDate: String,
    val tradeSessionDate: String
) {
    companion object {
        fun fromRow(row: List<String?>): DataVersion = DataVersion(
            dataVersion = row.getOrNull(0)?.toIntOrNull() ?: 0,
            seqnum = row.getOrNull(1)?.toLongOrNull() ?: 0L,
            tradeDate = row.getOrNull(2) ?: "",
            tradeSessionDate = row.getOrNull(3) ?: ""
        )
    }
}