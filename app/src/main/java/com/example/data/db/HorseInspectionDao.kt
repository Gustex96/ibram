package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HorseInspection
import kotlinx.coroutines.flow.Flow

@Dao
interface HorseInspectionDao {
    @Query("SELECT * FROM horse_inspections ORDER BY captureTimestamp DESC")
    fun getAllInspections(): Flow<List<HorseInspection>>

    @Query("SELECT * FROM horse_inspections ORDER BY captureTimestamp DESC")
    suspend fun getAllInspectionsList(): List<HorseInspection>

    @Query("SELECT * FROM horse_inspections WHERE id = :id LIMIT 1")
    fun getInspectionById(id: Long): Flow<HorseInspection?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: HorseInspection): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(inspections: List<HorseInspection>)

    @Update
    suspend fun updateInspection(inspection: HorseInspection)

    @Delete
    suspend fun deleteInspection(inspection: HorseInspection)

    @Query("DELETE FROM horse_inspections WHERE id = :id")
    suspend fun deleteInspectionById(id: Long)
}
