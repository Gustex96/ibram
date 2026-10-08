package com.example.data.repository

import com.example.data.db.HorseInspectionDao
import com.example.data.model.HorseInspection
import kotlinx.coroutines.flow.Flow

class InspectionRepository(private val dao: HorseInspectionDao) {
    val allInspections: Flow<List<HorseInspection>> = dao.getAllInspections()

    fun getInspectionById(id: Long): Flow<HorseInspection?> = dao.getInspectionById(id)

    suspend fun insertInspection(inspection: HorseInspection): Long = dao.insertInspection(inspection)

    suspend fun updateInspection(inspection: HorseInspection) = dao.updateInspection(inspection)

    suspend fun deleteInspection(inspection: HorseInspection) = dao.deleteInspection(inspection)

    suspend fun deleteInspectionById(id: Long) = dao.deleteInspectionById(id)
}
