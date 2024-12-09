package com.example.directedsonarapp.ui.screens

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.directedsonarapp.data.database.DatabaseProvider
import com.example.directedsonarapp.viewmodel.GraphViewModel
import com.example.directedsonarapp.viewmodel.GraphViewModelFactory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GraphScreen() {
    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context)
    val dao = db.measurementDao()
    val viewModel: GraphViewModel = viewModel(factory = GraphViewModelFactory(dao))

    var currentPage by remember { mutableStateOf(0) }

    val measurements = viewModel.measurements.collectAsStateWithLifecycle(initialValue = emptyList())

    val groupedMeasurements = measurements.value.groupBy { it.note.orEmpty() }
    val pages = groupedMeasurements.keys.toList()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (pages.isNotEmpty()) {
            val currentKey = pages[currentPage]
            val currentMeasurements = groupedMeasurements[currentKey] ?: emptyList()
            val entries = currentMeasurements.mapIndexed { index, measurement ->
                BarEntry(index.toFloat(), measurement.distance.toFloat()) // Преобразование Double в Float
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                factory = { context ->
                    BarChart(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { chart ->
                    if (entries.isNotEmpty()) {
                        val dataSet = BarDataSet(entries, "$currentKey").apply {
                            color = Color.BLUE
                            valueTextColor = Color.TRANSPARENT
                            valueTextSize = 0f
                        }

                        val barData = BarData(dataSet)
                        chart.data = barData

                        chart.axisLeft.removeAllLimitLines()

                        val median = currentMeasurements.map { it.distance.toFloat() }.sorted().let { sortedList ->
                            if (sortedList.isEmpty()) 0f
                            else if (sortedList.size % 2 == 0) {
                                (sortedList[sortedList.size / 2 - 1] + sortedList[sortedList.size / 2]) / 2
                            } else sortedList[sortedList.size / 2]
                        }

                        chart.axisLeft.addLimitLine(
                            com.github.mikephil.charting.components.LimitLine(median, "Median: %.2f".format(median)).apply {
                                lineColor = Color.RED
                                lineWidth = 5f
                                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD) // Жирный текст
                                textColor = Color.RED
                                textSize = 16f
                                enableDashedLine(10f, 15f, 0f)
                            }
                        )

                        chart.xAxis.apply {
                            textColor = Color.GRAY
                            textSize = 12f
                            setDrawGridLines(false)
                            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                            valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                                currentMeasurements.map {
                                    SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date(it.timestamp))
                                }
                            )
                        }

                        chart.axisLeft.apply {
                            textColor = Color.GRAY
                            textSize = 12f
                            axisMinimum = 0f
                            granularity = 0.05f
                            axisMaximum = ((entries.maxOfOrNull { it.y } ?: 0f) + 0.05f).coerceAtLeast(0.05f)

                            val medianLimitLine = com.github.mikephil.charting.components.LimitLine(median, "%.2f".format(median)).apply {
                                lineColor = Color.TRANSPARENT
                                lineWidth = 2f
                                labelPosition = com.github.mikephil.charting.components.LimitLine.LimitLabelPosition.LEFT_TOP
                                textSize = 16f
                                textColor = Color.RED
                            }
                            addLimitLine(medianLimitLine)
                        }

                        chart.axisRight.isEnabled = false

                        chart.description.isEnabled = false

                        chart.legend.apply {
                            isEnabled = true
                            verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                            horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                            orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                            textSize = 16f
                            textColor = Color.BLUE
                            xEntrySpace = 8f
                            yEntrySpace = 8f
                            form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                            formSize = 10f
                            formToTextSpace = 8f
                            direction = com.github.mikephil.charting.components.Legend.LegendDirection.LEFT_TO_RIGHT

                            chart.setExtraOffsets(16f, 0f, 16f, 0f)
                        }

                        chart.invalidate()
                    } else {
                        chart.clear()
                        chart.setNoDataText("No data available")
                        chart.setNoDataTextColor(Color.GRAY)
                    }
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PaginationButton(
                text = "Previous",
                onClick = { if (currentPage > 0) currentPage-- },
                enabled = currentPage > 0
            )
            Text(
                text = "Page ${currentPage + 1} of ${pages.size}",
                modifier = Modifier.align(Alignment.CenterVertically),
                style = androidx.compose.material.MaterialTheme.typography.body1.copy(
                    fontSize = 14.sp,
                    color = ComposeColor.Gray
                )
            )
            PaginationButton(
                text = "Next",
                onClick = { if (currentPage < pages.size - 1) currentPage++ },
                enabled = currentPage < pages.size - 1
            )
        }
    }
}

@Composable
fun PaginationButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .padding(8.dp)
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (enabled) ComposeColor(0xFF3700B3) else ComposeColor.Gray,
            contentColor = ComposeColor.White
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = text,
            style = androidx.compose.material.MaterialTheme.typography.button.copy(
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )
    }
}
