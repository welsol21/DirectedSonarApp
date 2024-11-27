package com.example.directedsonarapp.viewmodel

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.directedsonarapp.data.database.Measurement
import com.example.directedsonarapp.data.database.MeasurementDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class HomeViewModel(private val dao: MeasurementDao) : ViewModel() {

    fun startMeasurement(note: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val distance = measureDistance()
                val timestamp = System.currentTimeMillis()

                val measurement = Measurement(distance = distance, timestamp = timestamp, note = note)
                dao.insert(measurement)

                launch(Dispatchers.Main) {
                    onComplete(true, "Measurement saved successfully!")
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onComplete(false, "Failed to save measurement: ${e.message}")
                }
            }
        }
    }


    private fun measureDistance(): Double {
        val sampleRate = 44100
        val duration = 1
        val frequency = 20000.0

        val buffer = ShortArray(sampleRate * duration)
        for (i in buffer.indices) {
            buffer[i] = (Short.MAX_VALUE * kotlin.math.sin(2 * Math.PI * frequency * i / sampleRate)).toInt().toShort()
        }

        val audioTrack = AudioTrack(
            AudioTrack.MODE_STATIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            buffer.size * 2,
            AudioTrack.MODE_STATIC
        )
        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            buffer.size * 2
        )
        val recordedBuffer = ShortArray(buffer.size)
        audioRecord.startRecording()
        audioRecord.read(recordedBuffer, 0, recordedBuffer.size)
        audioRecord.stop()
        audioRecord.release()

        audioTrack.stop()
        audioTrack.release()

        val delayInSamples = findDelay(buffer, recordedBuffer)
        val delayInSeconds = delayInSamples / sampleRate.toDouble()

        return (delayInSeconds * 343) / 2
    }

    private fun findDelay(transmitted: ShortArray, recorded: ShortArray): Int {
        for (i in recorded.indices) {
            if (i + transmitted.size < recorded.size) {
                var match = true
                for (j in transmitted.indices) {
                    if (recorded[i + j] != transmitted[j]) {
                        match = false
                        break
                    }
                }
                if (match) return i
            }
        }
        return 0
    }
}

class HomeViewModelFactory(private val dao: MeasurementDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
