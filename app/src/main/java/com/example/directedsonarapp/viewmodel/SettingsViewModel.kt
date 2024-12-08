package com.example.directedsonarapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

    private val _signalCount = MutableLiveData(sharedPreferences.getInt("signal_count", 1))
    val signalCount: LiveData<Int> get() = _signalCount

    private val _signalDuration = MutableLiveData(sharedPreferences.getInt("signal_duration", 3))
    val signalDuration: LiveData<Int> get() = _signalDuration

    private val _sampleRate = MutableLiveData(sharedPreferences.getInt("sample_rate", 48000))
    val sampleRate: LiveData<Int> get() = _sampleRate

    private val _frequency = MutableLiveData(sharedPreferences.getInt("frequency", 440))
    val frequency: LiveData<Int> get() = _frequency

    fun setSignalCount(value: Int) {
        _signalCount.value = value
        sharedPreferences.edit().putInt("signal_count", value).apply()
    }

    fun setSignalDuration(value: Int) {
        _signalDuration.value = value
        sharedPreferences.edit().putInt("signal_duration", value).apply()
    }

    fun setSampleRate(value: Int) {
        _sampleRate.value = value
        sharedPreferences.edit().putInt("sample_rate", value).apply()
    }

    fun setFrequency(value: Int) {
        _frequency.value = value
        sharedPreferences.edit().putInt("frequency", value).apply()
    }
}
