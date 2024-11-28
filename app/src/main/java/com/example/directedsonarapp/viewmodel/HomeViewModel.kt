package com.example.directedsonarapp.viewmodel

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.directedsonarapp.data.database.Measurement
import com.example.directedsonarapp.data.database.MeasurementDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val dao: MeasurementDao) : ViewModel() {

    fun startMeasurement(context: Context, note: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Передаём context в measureDistance
                val distance = measureDistance(context)
                val timestamp = System.currentTimeMillis()

                val measurement = Measurement(distance = distance, timestamp = timestamp, note = note)
                dao.insert(measurement)

                launch(Dispatchers.Main) {
                    onComplete(true, "Measurement saved successfully! Distance: ${"%.2f".format(distance)} m")
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onComplete(false, "Failed to save measurement: ${e.message}")
                }
            }
        }
    }

    private fun measureDistance(context: Context): Double {
        val sampleRate = 44100
        val duration = 5
        val frequency = 8000.0

        val buffer = ShortArray(sampleRate * duration)
        for (i in buffer.indices) {
            buffer[i] = (Short.MAX_VALUE * kotlin.math.sin(2 * Math.PI * frequency * i / sampleRate)).toInt().toShort()
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )

        val audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            buffer.size * 2,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        audioTrack.setVolume(AudioTrack.getMaxVolume())
        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        Thread.sleep(duration * 1000L)

        audioTrack.stop()
        audioTrack.release()

        return 0.0
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

    fun playTestTone(context: Context) {
        val sampleRate = 44100
        val frequency = 1000.0
        val duration = 2

        val buffer = ShortArray(sampleRate * duration)
        for (i in buffer.indices) {
            buffer[i] = (Short.MAX_VALUE * kotlin.math.sin(2 * Math.PI * frequency * i / sampleRate)).toInt().toShort()
        }

        val audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            buffer.size * 2,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        audioTrack.setVolume(AudioTrack.getMaxVolume())
        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        Thread.sleep(2000) // Play for 2 seconds
        audioTrack.stop()
        audioTrack.release()
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
