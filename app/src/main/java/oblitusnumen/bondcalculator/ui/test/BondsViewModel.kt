//package oblitusnumen.bondcalculator.ui.model
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import oblitusnumen.bondcalculator.impl.database.AppDatabase
//import oblitusnumen.bondcalculator.impl.moexapi.schema.Bond
//
//class BondsViewModel(
//    private val database: AppDatabase
//) : ViewModel() {
//
//    var bonds by mutableStateOf<List<Bond>>(emptyList())
//        private set
//
//    var selectedBond by mutableStateOf<Bond?>(null)
//        private set
//
//    init {
//        loadBonds()
//    }
//
//    fun loadBonds() {
//        viewModelScope.launch(Dispatchers.IO) {
//
//            val result =
//                database.bonds.findAll()
//
//            withContext(Dispatchers.Main) {
//                bonds = result
//            }
//        }
//    }
//
//    fun selectBond(
//        secid: String
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//
//            val bond =
//                database.bonds.findBySecId(secid)
//
//            withContext(Dispatchers.Main) {
//                selectedBond = bond
//            }
//        }
//    }
//
//    fun saveBond(
//        bond: Bond
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//
//            database.bonds.save(bond)
//
//            val result =
//                database.bonds.findAll()
//
//            withContext(Dispatchers.Main) {
//                bonds = result
//            }
//        }
//    }
//}