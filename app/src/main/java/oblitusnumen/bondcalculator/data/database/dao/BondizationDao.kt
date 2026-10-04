package oblitusnumen.bondcalculator.data.database.dao

import kotlinx.serialization.json.Json
import oblitusnumen.bondcalculator.data.schema.BondPayment
import java.io.File

class BondizationDao(val cacheDir: File) {
    fun saveBondization(bondSecId: String, bondization: List<BondPayment>) {
        getBondizationDir().mkdirs()
        val bondizationFile = getBondizationFile(bondSecId)
//        println("save bondization:${Json.encodeToString(bondization)}")
        bondizationFile.writeText(Json.encodeToString(bondization))
    }

    fun getBondization(bondSecId: String): List<BondPayment>? {
        getBondizationDir().mkdirs()
        val bondizationFile = getBondizationFile(bondSecId)
//        println("get bondization:${if (!bondizationFile.exists()) null else bondizationFile.readText()}")
        return if (!bondizationFile.exists()) null else Json.decodeFromString(bondizationFile.readText())
    }

    fun getBondizationFile(secId: String) =
        File(getBondizationDir(), secId)

    fun getBondizationDir(): File =
        File(cacheDir, BONDIZATION_DIR)

    companion object {
        const val BONDIZATION_DIR = "bondization"
    }
}