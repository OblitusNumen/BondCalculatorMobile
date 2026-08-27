package oblitusnumen.bondcalculator.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import oblitusnumen.bondcalculator.data.schema.FinanceParameters
import oblitusnumen.bondcalculator.data.schema.LocalBond
import java.net.URLEncoder
import java.time.LocalDate

fun urlEncode(value: String): String {
    return URLEncoder.encode(value, "UTF-8")
}

/**
 * puts value into the set associated with the key
 * @return number of values associated with key before addition
 */
fun <K, V> MutableMap<K, MutableSet<V>>.add(key: K, value: V): Int {
    putIfAbsent(key, mutableSetOf())
    this[key]?.add(value)
    return this[key]!!.size - 1
}

fun <K, V> MutableMap<K, MutableSet<V>>.remove(key: K, value: V) {
    this[key]?.remove(value)
    if (this[key]?.isEmpty() ?: false)
        remove(key)
}

fun String?.toLocalDateOrNull(): LocalDate? =
    this?.takeIf { it.isNotBlank() }
        ?.let { LocalDate.parse(it) }

const val DEFAULT_BOND_VALUE = 1000.0
const val DEFAULT_COUPON_PERIOD = 182
val DEPOSIT_COMMON_PERIODS = listOf(30, 61, 92, 122, 182, 274, 365, 547, 730, 912, 1095)

private const val SHARED_PREFERENCES_NAME: String = "all_preferences"
fun getSharedPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
}

const val SETTINGS_SHARED_PREFS = "settings_shared_prefs"
fun getSettings(context: Context) =
    FinanceParameters.fromString(getSharedPrefs(context).getString(SETTINGS_SHARED_PREFS, null))

fun putSettings(context: Context, settings: FinanceParameters) =
    getSharedPrefs(context).edit { putString(SETTINGS_SHARED_PREFS, settings.toString()) }

const val BONDS_SHARED_PREFS = "bonds_shared_prefs"
fun getBonds(context: Context): List<LocalBond> =
    getSharedPrefs(context).getStringSet(BONDS_SHARED_PREFS, emptySet<String>())!!
        .map { bond -> LocalBond.fromString(bond)!! }

fun putBonds(context: Context, bonds: Collection<LocalBond>) =
    getSharedPrefs(context).edit { putStringSet(BONDS_SHARED_PREFS, bonds.map { it.toString() }.toSet()) }

const val BOND_ID_SHARED_PREFS = "bond_id_shared_prefs"
fun getBondId(context: Context): Int =
    getSharedPrefs(context).getInt(BOND_ID_SHARED_PREFS, 0)

fun incBondId(context: Context) =
    getSharedPrefs(context).edit { putInt(BOND_ID_SHARED_PREFS, getBondId(context) + 1) }

fun getBond(context: Context, bondId: Int?): LocalBond? =
    getBonds(context).firstOrNull { it.id == bondId }

fun saveBond(context: Context, bond: LocalBond) =
    putBonds(context, getBonds(context).toMutableSet().apply {
        removeIf { it.id == bond.id }
        add(bond)
    })

fun deleteBond(context: Context, bond: LocalBond) =
    putBonds(context, getBonds(context).toMutableSet().apply {
        removeIf { it.id == bond.id }
    })

const val DEPOSIT_VALUE_SHARED_PREFS = "deposit_value_shared_prefs"
const val DEFAULT_DEPOSIT_VALUE = 100000f
fun getDepositValue(context: Context) =
    getSharedPrefs(context).getFloat(DEPOSIT_VALUE_SHARED_PREFS, DEFAULT_DEPOSIT_VALUE).toDouble()

fun setDepositValue(context: Context, value: Double) =
    getSharedPrefs(context).edit { putFloat(DEPOSIT_VALUE_SHARED_PREFS, value.toFloat()) }

const val DEPOSIT_RATE_SHARED_PREFS = "deposit_rate_shared_prefs"
const val DEFAULT_DEPOSIT_RATE = 10f
fun getDepositRate(context: Context) =
    getSharedPrefs(context).getFloat(DEPOSIT_RATE_SHARED_PREFS, DEFAULT_DEPOSIT_RATE).toDouble()

fun setDepositRate(context: Context, value: Double) =
    getSharedPrefs(context).edit { putFloat(DEPOSIT_RATE_SHARED_PREFS, value.toFloat()) }

const val DEPOSIT_PERIOD_SHARED_PREFS = "deposit_period_shared_prefs"
const val DEFAULT_DEPOSIT_PERIOD = 30
fun getDepositPeriod(context: Context) =
    getSharedPrefs(context).getInt(DEPOSIT_PERIOD_SHARED_PREFS, DEFAULT_DEPOSIT_PERIOD)

fun setDepositPeriod(context: Context, value: Int) =
    getSharedPrefs(context).edit { putInt(DEPOSIT_PERIOD_SHARED_PREFS, value) }

const val BOND_FAVOURITES_SHARED_PREFS = "bond_favourites_shared_prefs"
val DEFAULT_BOND_FAVOURITES: Set<String> = emptySet()
fun getBondFavourites(context: Context): Set<String> =
    getSharedPrefs(context).getStringSet(BOND_FAVOURITES_SHARED_PREFS, DEFAULT_BOND_FAVOURITES)!!

fun setBondFavourites(context: Context, value: Set<String>) =
    getSharedPrefs(context).edit { putStringSet(BOND_FAVOURITES_SHARED_PREFS, value) }

