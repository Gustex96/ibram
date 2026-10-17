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
interface InspectionDao {
    @Query("SELECT * FROM inspections ORDER BY createdAt DESC")
    fun getAllInspections(): Flow<List<HorseInspection>>

    @Query("SELECT * FROM inspections WHERE id = :id LIMIT 1")
    suspend fun getInspectionById(id: Long): HorseInspection?

    @Query("SELECT * FROM inspections WHERE protocolNumber = :protocol LIMIT 1")
    suspend fun getInspectionByProtocol(protocol: String): HorseInspection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: HorseInspection): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(inspections: List<HorseInspection>)

    @Update
    suspend fun updateInspection(inspection: HorseInspection)

    @Delete
    suspend fun deleteInspection(inspection: HorseInspection)

    @Query("DELETE FROM inspections WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM inspections")
    suspend fun getCount(): Int
}
