package oblitusnumen.bondcalculator.data.schema

import kotlinx.serialization.Serializable

/**
 * Full details of a single bond as returned by the MOEX ISS API.
 * Fields are named exactly as in the `columns` array of the `/securities` endpoint.
 *
 * @property secid Unique security identifier (e.g., "SU26207RMFS9").
 * @property boardid Trading board code (e.g., "SPOB" – primary bond board).
 * @property shortname Short abbreviated name of the bond.
 * @property couponValue Absolute coupon value in the bond's currency (per bond,
 *   not per lot). For floating coupons this is the last known value.
 * @property accruedInt Accrued coupon interest (ACI) in the bond's currency,
 *   accumulated since the last coupon payment.
 * @property prevPrice Previous closing price (or last traded price).
 * @property lotSize Number of bonds in one standard lot (for trading).
 * @property faceValue Nominal (face) value of one bond.
 * @property boardName Full name of the trading board (e.g., "Поставка по ОФЗ").
 * @property matDate Maturity date in `yyyy-MM-dd` format.
 * @property decimals Number of decimal places used for price display.
 * @property couponPeriod Coupon period in days (e.g., 182 for semiannual).
 * @property issueSize Total emission volume in the bond's currency
 *   (total face value of all issued bonds).
 * @property secName Full Russian name of the security.
 * @property faceUnit Currency code for the face value (e.g., "SUR" for Russian ruble).
 * @property isin International Securities Identification Number (12‑character code).
 * @property latName Full name in Latin characters (English transliteration).
 * @property regNumber Registration number assigned by the regulator (e.g., "26207RMFS").
 * @property currencyId Currency code used for trading and settlement (e.g., "SUR").
 * @property issueSizePlaced Actual placed volume (can differ from total issue size).
 * @property couponPercent Annual coupon rate as a percentage (e.g., 8.15 for 8.15%).
 * @property lotValue Total face value of one lot (lotSize × faceValue).
 * @property callOptionDate Date of the next call option (issuer early redemption)
 *   in `yyyy-MM-dd` format.
 * @property putOptionDate Date of the next put option (holder early redemption)
 *   in `yyyy-MM-dd` format.
 * @property bondType Type of bond (e.g., "Фикс с известным купоном" = fixed coupon).
 * @property bondSubType Sub‑type of bond (e.g., "До погашения" = until maturity).
 */
@Serializable
data class Bond(
    val secid: String,                      // SECID
    val boardid: String?,                   // BOARDID
    val shortname: String?,                 // SHORTNAME
    val couponValue: Double?,               // COUPONVALUE
    val accruedInt: Double?,                // ACCRUEDINT
    val prevPrice: Double?,                 // PREVPRICE
    val lotSize: Int?,                      // LOTSIZE
    val faceValue: Double?,                 // FACEVALUE
    val boardName: String?,                 // BOARDNAME
    val matDate: String?,                   // MATDATE
    val decimals: Int?,                     // DECIMALS
    val couponPeriod: Int?,                 // COUPONPERIOD
    val issueSize: Long?,                   // ISSUESIZE (int64)
    val secName: String?,                   // SECNAME
    val faceUnit: String?,                  // FACEUNIT
    val isin: String?,                      // ISIN
    val latName: String?,                   // LATNAME
    val regNumber: String?,                 // REGNUMBER
    val currencyId: String?,                // CURRENCYID
    val issueSizePlaced: Long?,             // ISSUESIZEPLACED
    val couponPercent: Double?,             // COUPONPERCENT
    val lotValue: Double?,                  // LOTVALUE
    val callOptionDate: String?,            // CALLOPTIONDATE
    val putOptionDate: String?,             // PUTOPTIONDATE
    val bondType: String?,                  // BONDTYPE
    val bondSubType: String?                // BONDSUBTYPE
) {
    companion object {
        fun fromMap(map: Map<String, String?>): Bond {
            return Bond(
                secid = map["SECID"] ?: "",
                boardid = map["BOARDID"],
                shortname = map["SHORTNAME"],
                couponValue = map["COUPONVALUE"]?.toDoubleOrNull(),
                accruedInt = map["ACCRUEDINT"]?.toDoubleOrNull(),
                prevPrice = map["PREVPRICE"]?.toDoubleOrNull(),
                lotSize = map["LOTSIZE"]?.toIntOrNull(),
                faceValue = map["FACEVALUE"]?.toDoubleOrNull(),
                boardName = map["BOARDNAME"],
                matDate = map["MATDATE"],
                decimals = map["DECIMALS"]?.toIntOrNull(),
                couponPeriod = map["COUPONPERIOD"]?.toIntOrNull(),
                issueSize = map["ISSUESIZE"]?.toLongOrNull(),
                secName = map["SECNAME"],
                faceUnit = map["FACEUNIT"],
                isin = map["ISIN"],
                latName = map["LATNAME"],
                regNumber = map["REGNUMBER"],
                currencyId = map["CURRENCYID"],
                issueSizePlaced = map["ISSUESIZEPLACED"]?.toLongOrNull(),
                couponPercent = map["COUPONPERCENT"]?.toDoubleOrNull(),
                lotValue = map["LOTVALUE"]?.toDoubleOrNull(),
                callOptionDate = map["CALLOPTIONDATE"],
                putOptionDate = map["PUTOPTIONDATE"],
                bondType = map["BONDTYPE"],
                bondSubType = map["BONDSUBTYPE"]
            )
        }

        /**
         * Parses a raw row (list of values in the exact column order)
         * into a BondDetails instance.
         */
        fun fromRow(row: List<String?>): Bond {
            return Bond(
                secid = row.getOrNull(0) ?: "",
                boardid = row.getOrNull(1),
                shortname = row.getOrNull(2),
                couponValue = row.getOrNull(5)?.toDoubleOrNull(),
                accruedInt = row.getOrNull(7)?.toDoubleOrNull(),
                prevPrice = row.getOrNull(8)?.toDoubleOrNull(),
                lotSize = row.getOrNull(9)?.toIntOrNull(),
                faceValue = row.getOrNull(10)?.toDoubleOrNull(),
                boardName = row.getOrNull(11),
                matDate = row.getOrNull(13),
                decimals = row.getOrNull(14)?.toIntOrNull(),
                couponPeriod = row.getOrNull(15)?.toIntOrNull(),
                issueSize = row.getOrNull(16)?.toLongOrNull(),
                secName = row.getOrNull(19),
                faceUnit = row.getOrNull(25),
                isin = row.getOrNull(28),
                latName = row.getOrNull(29),
                regNumber = row.getOrNull(30),
                currencyId = row.getOrNull(31),
                issueSizePlaced = row.getOrNull(32)?.toLongOrNull(),
                couponPercent = row.getOrNull(35)?.toDoubleOrNull(),
                lotValue = row.getOrNull(38)?.toDoubleOrNull(),
                callOptionDate = row.getOrNull(40),
                putOptionDate = row.getOrNull(41),
                bondType = row.getOrNull(43),
                bondSubType = row.getOrNull(44)
            )
        }
    }
}