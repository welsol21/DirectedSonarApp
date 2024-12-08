package com.example.directedsonarapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.directedsonarapp.data.database.Measurement
import com.example.directedsonarapp.data.database.MeasurementDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.State

class HistoryViewModel(private val dao: MeasurementDao) : ViewModel() {

    val measurements = dao.getAllMeasurements()
    private val _filterText = mutableStateOf("")
    val filterText: State<String> = _filterText

    fun updateFilterText(newFilter: String) {
        _filterText.value = newFilter
    }

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
