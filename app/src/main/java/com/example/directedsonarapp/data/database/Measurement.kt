package com.example.directedsonarapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "distance_measurements")
data class Measurement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val distance: Double,
    val timestamp: Long,
    val note: String? = null
)
