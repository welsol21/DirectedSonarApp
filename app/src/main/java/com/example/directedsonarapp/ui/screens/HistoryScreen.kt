package com.example.directedsonarapp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.directedsonarapp.data.database.DatabaseProvider
import com.example.directedsonarapp.data.database.Measurement
import com.example.directedsonarapp.viewmodel.HistoryViewModel
import com.example.directedsonarapp.viewmodel.HistoryViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SortableHeader(
    text: String,
    field: String,
    currentSortField: String,
    ascending: Boolean,
    onSortChange: (String, Boolean) -> Unit
) {
    val width = when (field) {
        "datetime" -> 140.dp
        else -> 100.dp
    }

    Button(
        onClick = {
            if (currentSortField == field) {
                onSortChange(field, !ascending)
            } else {
                onSortChange(field, true)
            }
        },
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent)
    ) {
        Text(
            text = text + if (currentSortField == field) {
                if (ascending) " ↑" else " ↓"
            } else "",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@SuppressLint("SimpleDateFormat")
@Composable
fun MeasurementRow(
    measurement: Measurement,
    onUpdateNote: (String) -> Unit
) {
    val dateTime = Date(measurement.timestamp)
    val datetimeFormat = SimpleDateFormat("dd.MM.yy HH:mm:ss")

    var note by remember { mutableStateOf(TextFieldValue(measurement.note ?: "")) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "%.2f".format(measurement.distance),
            modifier = Modifier.width(100.dp).padding(horizontal = 4.dp), // Фиксированная ширина
            textAlign = TextAlign.Center
        )
        Text(
            text = datetimeFormat.format(dateTime),
            modifier = Modifier.width(140.dp).padding(horizontal = 4.dp), // Фиксированная ширина
            textAlign = TextAlign.Center
        )
        TextField(
            value = note,
            onValueChange = { newValue ->
                note = newValue
                onUpdateNote(newValue.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            textStyle = TextStyle(fontSize = 14.sp)
        )
    }
}

@Composable
fun HistoryScreen(context: android.content.Context) {
    val db = DatabaseProvider.getDatabase(context)
    val dao = db.measurementDao()
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(dao))

    val measurements by viewModel.measurements.observeAsState(emptyList())
    val itemsPerPage = 10
    var currentPage by remember { mutableStateOf(0) }
    var sortField by remember { mutableStateOf("distance") }
    var ascending by remember { mutableStateOf(true) }

    val sortedMeasurements = remember(measurements, sortField, ascending) {
        when (sortField) {
            "distance" -> if (ascending) measurements.sortedBy { it.distance } else measurements.sortedByDescending { it.distance }
            "datetime" -> if (ascending) measurements.sortedBy { it.timestamp } else measurements.sortedByDescending { it.timestamp }
            "note" -> if (ascending) measurements.sortedBy { it.note } else measurements.sortedByDescending { it.note }
            else -> measurements
        }
    }

    val paginatedMeasurements = sortedMeasurements.chunked(itemsPerPage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Table header with sorting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SortableHeader("Distance (m)", "distance", sortField, ascending) { field, asc ->
                sortField = field
                ascending = asc
            }
            SortableHeader("Datetime", "datetime", sortField, ascending) { field, asc ->
                sortField = field
                ascending = asc
            }
            SortableHeader("Note", "note", sortField, ascending) { field, asc ->
                sortField = field
                ascending = asc
            }
        }

        // LazyColumn for paginated data
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .padding(vertical = 8.dp)
        ) {
            val currentItems = paginatedMeasurements.getOrNull(currentPage) ?: emptyList()

            itemsIndexed(currentItems) { _, measurement ->
                MeasurementRow(
                    measurement = measurement,
                    onUpdateNote = { newNote ->
                        viewModel.updateMeasurementNote(measurement, newNote)
                    }
                )
                Divider()
            }
        }

        // Pagination controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (currentPage > 0) currentPage-- },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF3700B3),
                    contentColor = Color.White
                ),
                enabled = currentPage > 0
            ) {
                Text("Previous")
            }
            Text(
                text = "Page ${currentPage + 1} of ${paginatedMeasurements.size}",
                modifier = Modifier.align(Alignment.CenterVertically),
                style = TextStyle(fontSize = 14.sp)
            )
            Button(
                onClick = { if (currentPage < paginatedMeasurements.size - 1) currentPage++ },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF3700B3),
                    contentColor = Color.White
                ),
                enabled = currentPage < paginatedMeasurements.size - 1
            ) {
                Text("Next")
            }
        }
    }
}
