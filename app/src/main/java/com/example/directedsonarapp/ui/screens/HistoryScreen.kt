package com.example.directedsonarapp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    val measurements by viewModel.measurements.collectAsStateWithLifecycle(initialValue = emptyList())

    val itemsPerPage = 10
    var currentPage by remember { mutableStateOf(0) }
    var sortField by remember { mutableStateOf("timestamp") }
    var ascending by remember { mutableStateOf(false) }

    val sortedMeasurements = remember(measurements, sortField, ascending) {
        when (sortField) {
            "distance" -> if (ascending) measurements.sortedBy { it.distance } else measurements.sortedByDescending { it.distance }
            "timestamp" -> if (ascending) measurements.sortedBy { it.timestamp } else measurements.sortedByDescending { it.timestamp }
            "note" -> if (ascending) measurements.sortedBy { it.note } else measurements.sortedByDescending { it.note }
            else -> measurements
        }
    }

    val paginatedMeasurements = sortedMeasurements.chunked(itemsPerPage)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            SortableHeader(
                text = "Distance",
                field = "distance",
                width = 100.dp,
                currentSortField = sortField,
                ascending = ascending,
                onSortChange = { field, asc ->
                    sortField = field
                    ascending = asc
                }
            )
            SortableHeader(
                text = "Datetime",
                field = "timestamp",
                width = 150.dp,
                currentSortField = sortField,
                ascending = ascending,
                onSortChange = { field, asc ->
                    sortField = field
                    ascending = asc
                }
            )
            SortableHeader(
                text = "Note",
                field = "note",
                width = 200.dp,
                currentSortField = sortField,
                ascending = ascending,
                onSortChange = { field, asc ->
                    sortField = field
                    ascending = asc
                }
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(top = 8.dp)
        ) {
            val currentItems = paginatedMeasurements.getOrNull(currentPage) ?: emptyList()
            itemsIndexed(currentItems) { _, measurement ->
                MeasurementRow(
                    measurement = measurement,
                    onUpdateNote = { newNote -> viewModel.updateMeasurementNote(measurement, newNote) }
                )
                Divider()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CustomButton(
                text = "Previous",
                onClick = { if (currentPage > 0) currentPage-- },
                enabled = currentPage > 0
            )
            Text(
                text = "Page ${currentPage + 1} of ${paginatedMeasurements.size}",
                modifier = Modifier.align(Alignment.CenterVertically),
                style = MaterialTheme.typography.body1.copy(fontSize = 14.sp)
            )
            CustomButton(
                text = "Next",
                onClick = { if (currentPage < paginatedMeasurements.size - 1) currentPage++ },
                enabled = currentPage < paginatedMeasurements.size - 1
            )
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
    val datetimeFormat = SimpleDateFormat("dd.MM.yy HH:mm:ss")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "%.2f".format(measurement.distance),
            modifier = Modifier
                .width(90.dp)
                .padding(horizontal = 4.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = datetimeFormat.format(dateTime),
            modifier = Modifier
                .width(160.dp)
                .padding(horizontal = 4.dp),
            textAlign = TextAlign.Center
        )
        TextField(
            value = measurement.note ?: "",
            onValueChange = { newNote ->
                onUpdateNote(newNote)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            textStyle = TextStyle(fontSize = 14.sp)
        )
    }
}

@Composable
fun SortableHeader(
    text: String,
    field: String,
    width: Dp,
    currentSortField: String,
    ascending: Boolean,
    onSortChange: (String, Boolean) -> Unit
) {
    val backgroundColor = if (currentSortField == field) Color(0xFF3700B3) else Color.Gray
    val textColor = Color.White

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
            .padding(horizontal = 2.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(16.dp) // Оставляем форму кнопок заголовков
    ) {
        Text(
            text = text + if (currentSortField == field) {
                if (ascending) " ↑" else " ↓"
            } else "",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.button.copy(
                fontSize = 14.sp, // Размер шрифта
                fontWeight = FontWeight.Bold,
                color = textColor // Цвет текста
            )
        )
    }
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .padding(horizontal = 8.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (enabled) Color(0xFF3700B3) else Color.Gray,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.button.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}