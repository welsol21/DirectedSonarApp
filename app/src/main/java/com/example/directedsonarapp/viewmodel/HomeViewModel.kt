package com.example.directedsonarapp.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.directedsonarapp.data.database.Measurement
import com.example.directedsonarapp.data.database.MeasurementDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.delay
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class HomeViewModel(application: Application, private val dao: MeasurementDao) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

    // Get settings values from SharedPreferences
    val signalCount: Int
        get() = sharedPreferences.getInt("signal_count", 1)

    val signalDuration: Int
        get() = sharedPreferences.getInt("signal_duration", 3)

    val sampleRate: Int
        get() = sharedPreferences.getInt("sample_rate", 48000)

    val frequency: Int
        get() = sharedPreferences.getInt("frequency", 440)

    fun startMeasurement(
        context: Context,
        note: String,
        onProgressUpdate: (Int) -> Unit,
        onComplete: (Boolean, String) -> Unit
    ) {
        val totalDuration = signalCount * signalDuration // Total duration for the entire series
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis()
                val distances = mutableListOf<Double>() // List to store results of measurements

                // Format the timestamp as a readable date and time
                val dateTime = SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

                // Generate the note
                val formattedNote = if (note.isBlank()) {
                    "${signalCount} x ${signalDuration} c, $sampleRate Hz, $frequency Hz: $dateTime"
                } else {
                    "$note: $dateTime"
                }

                // Start measurements in a loop
                for (currentSignal in 1..signalCount) {
                    // Perform a single measurement
                    val distance = measureDistance(context)
                    distances.add(distance)

                    // Update the progress bar for the overall series
                    onProgressUpdate(totalDuration - (signalCount - currentSignal) * signalDuration)
                }

                // Save the results
                val measurement = Measurement(
                    distance = distances.average(), // Save the average distance
                    timestamp = timestamp,
                    note = formattedNote
                )
                dao.insert(measurement)

                launch(Dispatchers.Main) {
                    onComplete(
                        true,
                        "Measurement saved successfully!\nAverage Distance: ${"%.2f".format(distances.average())} m"
                    )
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Log.e("AudioDebug", "Failed to save measurement: ${e.message}", e)
                    onComplete(false, "Failed to save measurement: ${e.message}")
                }
            }
        }
    }

    private fun measureDistance(context: Context): Double {
        val sampleRate = this.sampleRate
        val duration = this.signalDuration
        val frequency = this.frequency.toDouble()

        val buffer = ShortArray(sampleRate * duration)
        for (i in buffer.indices) {
            buffer[i] = (Short.MAX_VALUE * sin(2 * PI * frequency * i / sampleRate)).toInt().toShort()
        }

        Log.d("AudioDebug", "Generated signal size: ${buffer.size} samples")

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = minBufferSize * 2
        val recordedBuffer = ShortArray(bufferSize)

        val audioRecord = createAudioRecord(sampleRate, minBufferSize)
        if (audioRecord == null) {
            Log.e("AudioDebug", "AudioRecord creation failed")
            return 0.0
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

        try {
            audioRecord.startRecording()
            Log.d("AudioDebug", "Recording started")

            audioTrack.play()
            Log.d("AudioDebug", "Playback started")

            var samplesRead = 0
            val totalSamples = sampleRate * duration
            while (samplesRead < totalSamples) {
                val readResult = audioRecord.read(recordedBuffer, 0, recordedBuffer.size)
                if (readResult > 0) {
                    samplesRead += readResult
                    Log.d("AudioDebug", "Samples read: $samplesRead")
                } else {
                    Log.e("AudioDebug", "Error reading audio data")
                    break
                }
            }

            audioTrack.stop()
            audioRecord.stop()

        } finally {
            audioTrack.release()
            audioRecord.release()
        }

        val delayInSamples = findDelayUsingCrossCorrelation(buffer, recordedBuffer)
        val delayInSeconds = delayInSamples / sampleRate.toDouble()
        Log.d("AudioDebug", "Delay in samples: $delayInSamples")

        return (delayInSeconds * 343) / 2
    }

    private fun createAudioRecord(sampleRate: Int, minBufferSize: Int): AudioRecord? {
        val audioSource = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            MediaRecorder.AudioSource.UNPROCESSED
        } else {
            MediaRecorder.AudioSource.VOICE_RECOGNITION
        }

        if (getApplication<Application>().applicationContext.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            val audioRecord = AudioRecord(
                audioSource,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize
            )
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                Log.d("AudioDebug", "AudioRecord initialized successfully")
                return audioRecord
            } else {
                Log.e("AudioDebug", "Failed to initialize AudioRecord")
                return null
            }
        } else {
            val context = getApplication<Application>().applicationContext
            Toast.makeText(context, "Please grant RECORD_AUDIO permission in settings.", Toast.LENGTH_LONG).show()
            Log.e("AudioDebug", "Permission RECORD_AUDIO not granted")
            return null
        }
    }

    private fun findDelayUsingCrossCorrelation(transmitted: ShortArray, recorded: ShortArray): Int {
        // Ensure recorded array is not larger than transmitted array
        if (recorded.size > transmitted.size) {
            Log.e("AudioDebug", "Recorded array size is larger than transmitted array size")
            return 0
        }

        val correlationSize = recorded.size

        val correlation = DoubleArray(correlationSize)
        for (i in 0 until correlationSize) {
            var sum = 0.0
            for (j in 0 until transmitted.size) {
                // Ensure index (i + j) is within the bounds of the recorded array
                if (i + j < recorded.size) {
                    sum += transmitted[j] * recorded[i + j]
                } else {
                    break // Stop the inner loop if index goes out of bounds
                }
            }
            correlation[i] = sum
        }

        val maxCorrelationIndex = correlation.indices.maxByOrNull { correlation[it] } ?: 0
        Log.d("AudioDebug", "Max correlation index: $maxCorrelationIndex, Correlation value: ${correlation[maxCorrelationIndex]}")

        return maxCorrelationIndex
    }
}

class HomeViewModelFactory(private val application: Application, private val dao: MeasurementDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application, dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
