package com.example.directedsonarapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.directedsonarapp.data.database.Measurement
import com.example.directedsonarapp.data.database.MeasurementDao
import kotlinx.coroutines.flow.Flow

class GraphViewModel(private val dao: MeasurementDao) : ViewModel() {
    val measurements: Flow<List<Measurement>> = dao.getAllMeasurements()
}

class GraphViewModelFactory(private val dao: MeasurementDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GraphViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GraphViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
