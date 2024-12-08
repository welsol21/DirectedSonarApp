package com.example.directedsonarapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Insert
    suspend fun insert(measurement: Measurement)

    @Query("SELECT * FROM distance_measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<Measurement>>

    @Update
    fun update(measurement: Measurement)
}
