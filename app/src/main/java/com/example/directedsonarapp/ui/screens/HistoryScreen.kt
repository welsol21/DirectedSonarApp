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
fun HistoryScreen(context: android.content.Context) {
    val db = DatabaseProvider.getDatabase(context)
    val dao = db.measurementDao()
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(dao))

    val measurements by viewModel.measurements.observeAsState(emptyList())
    val itemsPerPage = 10
    var currentPage by remember { mutableStateOf(0) }

    val paginatedMeasurements = measurements.chunked(itemsPerPage)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Distance (m)", style = MaterialTheme.typography.subtitle1, modifier = Modifier.weight(1f))
            Text(text = "Date", style = MaterialTheme.typography.subtitle1, modifier = Modifier.weight(1f))
            Text(text = "Time", style = MaterialTheme.typography.subtitle1, modifier = Modifier.weight(1f))
            Text(text = "Note", style = MaterialTheme.typography.subtitle1, modifier = Modifier.weight(2f))
        }

        // LazyColumn for paginated data
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            val currentItems = paginatedMeasurements.getOrNull(currentPage) ?: emptyList()

            itemsIndexed(currentItems) { index, measurement ->
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
                    backgroundColor = Color(0xFF3700B3), // Same as Start button
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
                    backgroundColor = Color(0xFF3700B3), // Same as Start button
                    contentColor = Color.White
                ),
                enabled = currentPage < paginatedMeasurements.size - 1
            ) {
                Text("Next")
            }
        }
    }
}

@SuppressLint("SimpleDateFormat")
@Composable
fun MeasurementRow(
    measurement: Measurement,
    onUpdateNote: (String) -> Unit
) {
    val dateTime = Date(measurement.timestamp)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    val timeFormat = SimpleDateFormat("HH:mm:ss")

    var note by remember { mutableStateOf(TextFieldValue(measurement.note ?: "")) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "%.2f".format(measurement.distance),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = dateFormat.format(dateTime),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = timeFormat.format(dateTime),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        TextField(
            value = note,
            onValueChange = { newValue ->
                note = newValue
                onUpdateNote(newValue.text)
            },
            modifier = Modifier.weight(2f),
            textStyle = TextStyle(fontSize = 14.sp)
        )
    }
}
