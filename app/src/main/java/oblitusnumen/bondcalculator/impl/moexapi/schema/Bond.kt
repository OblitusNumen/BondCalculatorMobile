package oblitusnumen.bondcalculator.impl.moexapi.schema

import kotlinx.serialization.Serializable

/**
 * Full details of a single bond as returned by the MOEX ISS API.
 * Fields are named exactly as in the `columns` array of the `/securities` endpoint.
 *
 * @property secid Unique security identifier (e.g., "SU26207RMFS9").
 * @property boardid Trading board code (e.g., "SPOB" – primary bond board).
 * @property shortname Short abbreviated name of the bond.
 * @property prevwaprice Previous weighted average price (in the bond's currency).
 * @property yieldAtPrevWaprice Yield to maturity (or coupon yield) calculated
 *   using the previous weighted average price.
 * @property couponValue Absolute coupon value in the bond's currency (per bond,
 *   not per lot). For floating coupons this is the last known value.
 * @property nextCoupon Date of the next coupon payment in `yyyy-MM-dd` format.
 *   May be "0000-00-00" if unknown.
 * @property accruedInt Accrued coupon interest (ACI) in the bond's currency,
 *   accumulated since the last coupon payment.
 * @property prevPrice Previous closing price (or last traded price).
 * @property lotSize Number of bonds in one standard lot (for trading).
 * @property faceValue Nominal (face) value of one bond.
 * @property boardName Full name of the trading board (e.g., "Поставка по ОФЗ").
 * @property status Trading status code (e.g., "A" = active, "T" = trading halt).
 * @property matDate Maturity date in `yyyy-MM-dd` format.
 * @property decimals Number of decimal places used for price display.
 * @property couponPeriod Coupon period in days (e.g., 182 for semiannual).
 * @property issueSize Total emission volume in the bond's currency
 *   (total face value of all issued bonds).
 * @property prevLegalClosePrice Previous legal closing price (used for settlement).
 * @property prevDate Date of the previous trading session in `yyyy-MM-dd` format.
 * @property secName Full Russian name of the security.
 * @property remarks Additional remarks (often null).
 * @property marketCode Market code (e.g., "RPST" for government bonds).
 * @property instrId Instrument identifier used internally by MOEX.
 * @property sectorId Sector identifier (usually null for bonds).
 * @property minStep Minimum price step (tick size) in the bond's currency.
 * @property faceUnit Currency code for the face value (e.g., "SUR" for Russian ruble).
 * @property buybackPrice Price at which the issuer may buy back the bond.
 * @property buybackDate Date of the buyback offer in `yyyy-MM-dd` format.
 * @property isin International Securities Identification Number (12‑character code).
 * @property latName Full name in Latin characters (English transliteration).
 * @property regNumber Registration number assigned by the regulator (e.g., "26207RMFS").
 * @property currencyId Currency code used for trading and settlement (e.g., "SUR").
 * @property issueSizePlaced Actual placed volume (can differ from total issue size).
 * @property listLevel Listing level (1, 2, 3, or null) indicating the exchange's listing tier.
 * @property secType Security type code (e.g., "3" for bonds).
 * @property couponPercent Annual coupon rate as a percentage (e.g., 8.15 for 8.15%).
 * @property offerDate Date of the next offer (if any) in `yyyy-MM-dd` format.
 * @property settleDate Settlement date for the current trade in `yyyy-MM-dd` format.
 * @property lotValue Total face value of one lot (lotSize × faceValue).
 * @property faceValueOnSettleDate Face value adjusted for inflation/amortization
 *   at the settlement date (if applicable).
 * @property callOptionDate Date of the next call option (issuer early redemption)
 *   in `yyyy-MM-dd` format.
 * @property putOptionDate Date of the next put option (holder early redemption)
 *   in `yyyy-MM-dd` format.
 * @property dateYieldFromIssuer Date from which the current yield is calculated
 *   (issued‑by‑issuer yield) in `yyyy-MM-dd` format.
 * @property bondType Type of bond (e.g., "Фикс с известным купоном" = fixed coupon).
 * @property bondSubType Sub‑type of bond (e.g., "До погашения" = until maturity).
 */
@Serializable
data class Bond(
    val secid: String,                      // SECID
    val boardid: String?,                   // BOARDID
    val shortname: String?,                 // SHORTNAME
    val prevwaprice: Double?,               // PREVWAPRICE
    val yieldAtPrevWaprice: Double?,        // YIELDATPREVWAPRICE
    val couponValue: Double?,               // COUPONVALUE
    val nextCoupon: String?,                // NEXTCOUPON (date as string)
    val accruedInt: Double?,                // ACCRUEDINT
    val prevPrice: Double?,                 // PREVPRICE
    val lotSize: Int?,                      // LOTSIZE
    val faceValue: Double?,                 // FACEVALUE
    val boardName: String?,                 // BOARDNAME
    val status: String?,                    // STATUS
    val matDate: String?,                   // MATDATE
    val decimals: Int?,                     // DECIMALS
    val couponPeriod: Int?,                 // COUPONPERIOD
    val issueSize: Long?,                   // ISSUESIZE (int64)
    val prevLegalClosePrice: Double?,       // PREVLEGALCLOSEPRICE
    val prevDate: String?,                  // PREVDATE
    val secName: String?,                   // SECNAME
    val remarks: String?,                   // REMARKS
    val marketCode: String?,                // MARKETCODE
    val instrId: String?,                   // INSTRID
    val sectorId: String?,                  // SECTORID
    val minStep: Double?,                   // MINSTEP
    val faceUnit: String?,                  // FACEUNIT
    val buybackPrice: Double?,              // BUYBACKPRICE
    val buybackDate: String?,               // BUYBACKDATE
    val isin: String?,                      // ISIN
    val latName: String?,                   // LATNAME
    val regNumber: String?,                 // REGNUMBER
    val currencyId: String?,                // CURRENCYID
    val issueSizePlaced: Long?,             // ISSUESIZEPLACED
    val listLevel: Int?,                    // LISTLEVEL
    val secType: String?,                   // SECTYPE
    val couponPercent: Double?,             // COUPONPERCENT
    val offerDate: String?,                 // OFFERDATE
    val settleDate: String?,                // SETTLEDATE
    val lotValue: Double?,                  // LOTVALUE
    val faceValueOnSettleDate: Double?,     // FACEVALUEONSETTLEDATE
    val callOptionDate: String?,            // CALLOPTIONDATE
    val putOptionDate: String?,             // PUTOPTIONDATE
    val dateYieldFromIssuer: String?,       // DATEYIELDFROMISSUER
    val bondType: String?,                  // BONDTYPE
    val bondSubType: String?                // BONDSUBTYPE
) {

    companion object {
        /**
         * Builds a BondDetails instance from a map where keys are column names
         * (exactly as returned in the 'columns' array) and values are the corresponding
         * string values from the data row.
         */
        fun fromMap(map: Map<String, String?>): Bond {
            return Bond(
                secid = map["SECID"] ?: "",
                boardid = map["BOARDID"],
                shortname = map["SHORTNAME"],
                prevwaprice = map["PREVWAPRICE"]?.toDoubleOrNull(),
                yieldAtPrevWaprice = map["YIELDATPREVWAPRICE"]?.toDoubleOrNull(),
                couponValue = map["COUPONVALUE"]?.toDoubleOrNull(),
                nextCoupon = map["NEXTCOUPON"],
                accruedInt = map["ACCRUEDINT"]?.toDoubleOrNull(),
                prevPrice = map["PREVPRICE"]?.toDoubleOrNull(),
                lotSize = map["LOTSIZE"]?.toIntOrNull(),
                faceValue = map["FACEVALUE"]?.toDoubleOrNull(),
                boardName = map["BOARDNAME"],
                status = map["STATUS"],
                matDate = map["MATDATE"],
                decimals = map["DECIMALS"]?.toIntOrNull(),
                couponPeriod = map["COUPONPERIOD"]?.toIntOrNull(),
                issueSize = map["ISSUESIZE"]?.toLongOrNull(),
                prevLegalClosePrice = map["PREVLEGALCLOSEPRICE"]?.toDoubleOrNull(),
                prevDate = map["PREVDATE"],
                secName = map["SECNAME"],
                remarks = map["REMARKS"],
                marketCode = map["MARKETCODE"],
                instrId = map["INSTRID"],
                sectorId = map["SECTORID"],
                minStep = map["MINSTEP"]?.toDoubleOrNull(),
                faceUnit = map["FACEUNIT"],
                buybackPrice = map["BUYBACKPRICE"]?.toDoubleOrNull(),
                buybackDate = map["BUYBACKDATE"],
                isin = map["ISIN"],
                latName = map["LATNAME"],
                regNumber = map["REGNUMBER"],
                currencyId = map["CURRENCYID"],
                issueSizePlaced = map["ISSUESIZEPLACED"]?.toLongOrNull(),
                listLevel = map["LISTLEVEL"]?.toIntOrNull(),
                secType = map["SECTYPE"],
                couponPercent = map["COUPONPERCENT"]?.toDoubleOrNull(),
                offerDate = map["OFFERDATE"],
                settleDate = map["SETTLEDATE"],
                lotValue = map["LOTVALUE"]?.toDoubleOrNull(),
                faceValueOnSettleDate = map["FACEVALUEONSETTLEDATE"]?.toDoubleOrNull(),
                callOptionDate = map["CALLOPTIONDATE"],
                putOptionDate = map["PUTOPTIONDATE"],
                dateYieldFromIssuer = map["DATEYIELDFROMISSUER"],
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
                prevwaprice = row.getOrNull(3)?.toDoubleOrNull(),
                yieldAtPrevWaprice = row.getOrNull(4)?.toDoubleOrNull(),
                couponValue = row.getOrNull(5)?.toDoubleOrNull(),
                nextCoupon = row.getOrNull(6),
                accruedInt = row.getOrNull(7)?.toDoubleOrNull(),
                prevPrice = row.getOrNull(8)?.toDoubleOrNull(),
                lotSize = row.getOrNull(9)?.toIntOrNull(),
                faceValue = row.getOrNull(10)?.toDoubleOrNull(),
                boardName = row.getOrNull(11),
                status = row.getOrNull(12),
                matDate = row.getOrNull(13),
                decimals = row.getOrNull(14)?.toIntOrNull(),
                couponPeriod = row.getOrNull(15)?.toIntOrNull(),
                issueSize = row.getOrNull(16)?.toLongOrNull(),
                prevLegalClosePrice = row.getOrNull(17)?.toDoubleOrNull(),
                prevDate = row.getOrNull(18),
                secName = row.getOrNull(19),
                remarks = row.getOrNull(20),
                marketCode = row.getOrNull(21),
                instrId = row.getOrNull(22),
                sectorId = row.getOrNull(23),
                minStep = row.getOrNull(24)?.toDoubleOrNull(),
                faceUnit = row.getOrNull(25),
                buybackPrice = row.getOrNull(26)?.toDoubleOrNull(),
                buybackDate = row.getOrNull(27),
                isin = row.getOrNull(28),
                latName = row.getOrNull(29),
                regNumber = row.getOrNull(30),
                currencyId = row.getOrNull(31),
                issueSizePlaced = row.getOrNull(32)?.toLongOrNull(),
                listLevel = row.getOrNull(33)?.toIntOrNull(),
                secType = row.getOrNull(34),
                couponPercent = row.getOrNull(35)?.toDoubleOrNull(),
                offerDate = row.getOrNull(36),
                settleDate = row.getOrNull(37),
                lotValue = row.getOrNull(38)?.toDoubleOrNull(),
                faceValueOnSettleDate = row.getOrNull(39)?.toDoubleOrNull(),
                callOptionDate = row.getOrNull(40),
                putOptionDate = row.getOrNull(41),
                dateYieldFromIssuer = row.getOrNull(42),
                bondType = row.getOrNull(43),
                bondSubType = row.getOrNull(44)
            )
        }
    }
}