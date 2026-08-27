package oblitusnumen.bondcalculator.data.schema

/**
 * Summary of a security from search results.
 */
data class SecuritySummary(
    val secid: String,
    val group: String?,
    val type: String?,
    val isin: String?,
    val shortname: String?,
    val name: String?
)