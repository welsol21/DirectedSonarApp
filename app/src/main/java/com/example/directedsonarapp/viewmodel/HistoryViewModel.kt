package com.example.directedsonarapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.example.directedsonarapp.data.database.Measurement
import com.example.directedsonarapp.data.database.MeasurementDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class HistoryViewModel(private val dao: MeasurementDao) : ViewModel() {
    val measurements = liveData(Dispatchers.IO) {
        emit(dao.getAllMeasurements())
    }

    // Update function for editing the "note" field
    fun updateMeasurementNote(measurement: Measurement, newNote: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMeasurement = measurement.copy(note = newNote)
            dao.update(updatedMeasurement)
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
