package com.example.directedsonarapp.viewmodel

import androidx.lifecycle.asLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.directedsonarapp.data.database.Measurement
import com.example.directedsonarapp.data.database.MeasurementDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val dao: MeasurementDao) : ViewModel() {

//    val measurements = dao.getAllMeasurements().asLiveData()
    val measurements = dao.getAllMeasurements()

    fun updateMeasurementNote(measurement: Measurement, newNote: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMeasurement = measurement.copy(note = newNote)
            dao.update(updatedMeasurement)
            println("Updated measurement: $updatedMeasurement")
        }
    }
}

class HistoryViewModelFactory(private val dao: MeasurementDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
