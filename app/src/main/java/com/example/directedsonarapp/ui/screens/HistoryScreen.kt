package com.example.directedsonarapp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search

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
    var isFiltering by remember { mutableStateOf(false) }
    var filterText by remember { mutableStateOf("") }

    val filteredMeasurements = remember(measurements, filterText) {
        if (filterText.isEmpty()) {
            measurements
        } else {
            measurements.filter { it.note?.contains(filterText, ignoreCase = true) == true }
        }
    }

    val sortedMeasurements = remember(filteredMeasurements, sortField, ascending) {
        when (sortField) {
            "distance" -> if (ascending) filteredMeasurements.sortedBy { it.distance } else filteredMeasurements.sortedByDescending { it.distance }
            "timestamp" -> if (ascending) filteredMeasurements.sortedBy { it.timestamp } else filteredMeasurements.sortedByDescending { it.timestamp }
            "note" -> if (ascending) filteredMeasurements.sortedBy { it.note } else filteredMeasurements.sortedByDescending { it.note }
            else -> filteredMeasurements
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
                width = 110.dp,
                currentSortField = sortField,
                ascending = ascending,
                onSortChange = { field, asc ->
                    sortField = field
                    ascending = asc
                }
            )
            NoteHeader(
                isFiltering = isFiltering,
                filterText = filterText,
                onFilterChange = { filterText = it },
                onToggleFiltering = { isFiltering = !isFiltering },
                sortField = sortField,
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

@Composable
fun NoteHeader(
    isFiltering: Boolean,
    filterText: String,
    onFilterChange: (String) -> Unit,
    onToggleFiltering: () -> Unit,
    sortField: String,
    ascending: Boolean,
    onSortChange: (String, Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (isFiltering) {
            TextField(
                value = filterText,
                onValueChange = { onFilterChange(it) },
                placeholder = {
                    Text(
                        text = "Filter by Note",
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        ),
                        textAlign = TextAlign.Center
                    )
                },
                modifier = Modifier
                    .height(48.dp)
                    .width(110.dp)
                    .background(Color(0xFF3700B3), shape = RoundedCornerShape(16.dp)),
                textStyle = MaterialTheme.typography.subtitle1.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    cursorColor = Color.LightGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        } else {
            SortableHeader(
                text = "Note",
                field = "note",
                width = 110.dp,
                currentSortField = sortField,
                ascending = ascending,
                onSortChange = onSortChange
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .padding(start = 2.dp)
                .background(Color.White, shape = RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { onToggleFiltering() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Toggle Filter",
                    tint = Color(0xFF3700B3),
                    modifier = Modifier.size(28.dp)
                )
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
    val dateFormat = SimpleDateFormat("dd.MM.yy")
    val timeFormat = SimpleDateFormat("HH:mm:ss")

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
        Column(
            modifier = Modifier
                .width(110.dp)
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dateFormat.format(dateTime),
                style = TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center)
            )
            Text(
                text = timeFormat.format(dateTime),
                style = TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center)
            )
        }
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
    onSortChange: (String, Boolean) -> Unit,
) {
    val backgroundColor = if (currentSortField == field) Color(0xFF3700B3) else Color.Gray
    val textColor = Color.White

    Button(
        onClick = {
            onSortChange(field, !ascending)
        },
        modifier = Modifier
            .width(width)
            .padding(horizontal = 2.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text + if (currentSortField == field) {
                if (ascending) " ↑" else " ↓"
            } else "",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.button.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
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