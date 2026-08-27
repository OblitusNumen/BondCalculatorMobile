//package oblitusnumen.bondcalculator.ui.model
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import oblitusnumen.bondcalculator.impl.database.AppDatabase
//
//class BondsViewModelFactory(
//    private val database: AppDatabase
//) : ViewModelProvider.Factory {
//
//    override fun <T : ViewModel> create(
//        modelClass: Class<T>
//    ): T {
//        if (modelClass.isAssignableFrom(
//                BondsViewModel::class.java
//            )
//        ) {
//            @Suppress("UNCHECKED_CAST")
//            return BondsViewModel(
//                database
//            ) as T
//        }
//
//        throw IllegalArgumentException(
//            "Unknown ViewModel class"
//        )
//    }
//}