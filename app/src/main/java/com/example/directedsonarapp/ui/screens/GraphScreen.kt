package com.example.directedsonarapp.ui.screens

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.foundation.background
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
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Canvas
import android.graphics.Paint
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.utils.ViewPortHandler

@Composable
fun GraphScreen() {
    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context)
    val dao = db.measurementDao()
    val viewModel: GraphViewModel = viewModel(factory = GraphViewModelFactory(dao))

    // Состояние для пагинации
    var currentPage by remember { mutableStateOf(0) }

    // Получаем данные измерений
    val measurements = viewModel.measurements.collectAsStateWithLifecycle(initialValue = emptyList())

    // Группируем измерения по Note
    val groupedMeasurements = measurements.value.groupBy { it.note.orEmpty() }
    val pages = groupedMeasurements.keys.toList()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (pages.isNotEmpty()) {
            val currentKey = pages[currentPage]
            val currentMeasurements = groupedMeasurements[currentKey] ?: emptyList()
            val entries = currentMeasurements.mapIndexed { index, measurement ->
                BarEntry(index.toFloat(), measurement.distance.toFloat()) // Преобразование Double в Float
            }

            // Рассчитываем медиану
            val median = currentMeasurements.map { it.distance.toFloat() }.sorted().let { sortedList ->
                if (sortedList.isEmpty()) 0f
                else if (sortedList.size % 2 == 0) {
                    (sortedList[sortedList.size / 2 - 1] + sortedList[sortedList.size / 2]) / 2
                } else sortedList[sortedList.size / 2]
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
                        val dataSet = BarDataSet(entries, "Measurements for $currentKey").apply {
                            color = Color.BLUE
                            valueTextColor = Color.WHITE
                            valueTextSize = 14f
                        }

                        chart.data = BarData(dataSet)

                        // Очищаем старые лимитные линии
                        chart.axisLeft.removeAllLimitLines()

                        // Рассчитываем медиану
                        val median = currentMeasurements.map { it.distance.toFloat() }.sorted().let { sortedList ->
                            if (sortedList.isEmpty()) 0f
                            else if (sortedList.size % 2 == 0) {
                                (sortedList[sortedList.size / 2 - 1] + sortedList[sortedList.size / 2]) / 2
                            } else sortedList[sortedList.size / 2]
                        }

                        // Добавляем линию медианы
                        chart.axisLeft.addLimitLine(com.github.mikephil.charting.components.LimitLine(median, "Median: %.2f".format(median)).apply {
                            lineColor = Color.RED
                            lineWidth = 2f
                            textColor = Color.RED
                            textSize = 12f
                            enableDashedLine(10f, 10f, 0f)
                        })

                        // Настраиваем ось X
                        chart.xAxis.apply {
                            textColor = Color.WHITE
                            textSize = 12f
                            setDrawGridLines(false)
                            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                            valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                                currentMeasurements.map {
                                    SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(it.timestamp))
                                }
                            )
                        }

                        // Настраиваем ось Y
                        chart.axisLeft.apply {
                            textColor = Color.WHITE
                            textSize = 12f
                            axisMinimum = 0f // Минимальное значение на оси Y
                        }

                        chart.axisRight.isEnabled = false // Отключаем правую ось

                        chart.description.isEnabled = false
                        chart.legend.textColor = Color.WHITE // Цвет текста легенды

                        chart.invalidate()
                    } else {
                        chart.clear()
                        chart.setNoDataText("No data available")
                    }
                }
            )
        }

        // Пагинация
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
                    color = ComposeColor.White
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

class CustomBarChartRenderer(
    chart: BarChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
    private val dataSet: BarDataSet
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE // Цвет текста
        textSize = 40f // Размер текста
        textAlign = Paint.Align.CENTER
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLUE // Цвет фона (круг)
    }

    override fun drawValues(c: Canvas?) {
        if (c == null) return

        val barBuffer = mBarBuffers[0] // Получение первого BarBuffer
        val phaseY = mAnimator.phaseY

        for (i in 0 until dataSet.entryCount) {
            val entry = dataSet.getEntryForIndex(i) as? BarEntry ?: continue

            val x = barBuffer.buffer[i * 4] + barBuffer.buffer[i * 4 + 2] / 2
            val y = entry.y * phaseY
            val pos = mViewPortHandler.contentTop() + y

            // Рисуем круг под текст
            c.drawCircle(x, pos - 40f, 35f, backgroundPaint)

            // Рисуем значение
            c.drawText(
                "%.1f".format(entry.y),
                x,
                pos - 40f,
                valuePaint
            )
        }
    }
}
