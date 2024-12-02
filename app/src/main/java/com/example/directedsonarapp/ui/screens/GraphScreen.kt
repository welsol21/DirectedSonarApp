package com.example.directedsonarapp.ui.screens

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.directedsonarapp.data.database.DatabaseProvider
import com.example.directedsonarapp.viewmodel.GraphViewModel
import com.example.directedsonarapp.viewmodel.GraphViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun GraphScreen(navController: NavController) {
    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context)
    val dao = db.measurementDao()
    val viewModel: GraphViewModel = viewModel(factory = GraphViewModelFactory(dao))

    val measurements by viewModel.measurements.observeAsState(emptyList())

    val entries = measurements.mapIndexed { index, measurement ->
        Entry(index.toFloat(), measurement.distance.toFloat())
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { chart ->
            if (entries.isNotEmpty()) {
                val dataSet = LineDataSet(entries, "Measured Data").apply {
                    color = Color.BLUE
                    valueTextColor = Color.BLACK
                }

                chart.data = LineData(dataSet)
                chart.invalidate()
            } else {
                chart.clear()
                chart.setNoDataText("No data available")
            }
        }
    )
}
