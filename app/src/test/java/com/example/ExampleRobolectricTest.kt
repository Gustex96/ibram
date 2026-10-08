package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.DfConstants
import com.example.data.model.HorseInspection
import com.example.util.CpfValidator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Levantamento Operacional", appName)
    }

    @Test
    fun testMultiPhotoPathsHandling() {
        val inspection = HorseInspection(
            protocolNumber = "DF-EQU-TEST-PHOTOS",
            photoPath = "/mock/path/photo1.jpg",
            additionalPhotoPaths = "/mock/path/photo2.jpg;;/mock/path/photo3.jpg",
            latitude = -15.7942,
            longitude = -47.8822,
            administrativeRegion = "Guará (RA X)"
        )
        val allPhotos = inspection.allPhotoPaths
        assertEquals(3, allPhotos.size)
        assertEquals("/mock/path/photo1.jpg", allPhotos[0])
        assertEquals("/mock/path/photo2.jpg", allPhotos[1])
        assertEquals("/mock/path/photo3.jpg", allPhotos[2])
    }

    @Test
    fun testCpfFormattingAndValidation() {
        val raw = "12345678909"
        val formatted = CpfValidator.format(raw)
        assertEquals("123.456.789-09", formatted)

        assertFalse(CpfValidator.isValid("11111111111"))
        assertFalse(CpfValidator.isValid("00000000000"))
        assertFalse(CpfValidator.isValid("123"))

        // CNPJ dynamic support
        val rawCnpj = "11222333000181"
        val formattedCnpj = CpfValidator.format(rawCnpj)
        assertEquals("11.222.333/0001-81", formattedCnpj)
        assertTrue(CpfValidator.isCnpj(rawCnpj))
        assertFalse(CpfValidator.isCnpj("12345678909"))
    }

    @Test
    fun testDfConstants() {
        assertEquals(35, DfConstants.REGIOES_ADMINISTRATIVAS.size)
        assertEquals(5, DfConstants.RISK_LEVELS.size)
    }

    @Test
    fun testAnexoFDataContent() {
        assertEquals(3, com.example.data.model.AnexoFData.categories.size)
        val normalCategory = com.example.data.model.AnexoFData.categories.find { it.id == "normal" }
        val dogsCategory = com.example.data.model.AnexoFData.categories.find { it.id == "caes" }
        val catsCategory = com.example.data.model.AnexoFData.categories.find { it.id == "gatos" }

        assertNotNull(normalCategory)
        assertNotNull(dogsCategory)
        assertNotNull(catsCategory)

        assertEquals("Animal Saudável", normalCategory?.title)
        assertEquals(9, normalCategory?.items?.size)

        assertEquals("Cuidados Básicos para Cães", dogsCategory?.title)
        assertEquals(10, dogsCategory?.items?.size)

        assertEquals("Cuidados Básicos para Gatos", catsCategory?.title)
        assertEquals(9, catsCategory?.items?.size)
    }

    @Test
    fun testHorseInspectionEntityInRoomDatabase() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val dao = db.horseInspectionDao()

        val inspection = HorseInspection(
            protocolNumber = "DF-EQU-TEST-001",
            photoPath = "/mock/path/photo.jpg",
            latitude = -15.7942,
            longitude = -47.8822,
            administrativeRegion = "Plano Piloto (RA I)",
            horseDescription = "Cavalo manga-larga marchador, castanho, porte grande, sem ferimentos",
            horseCount = 2,
            mistreatedHorseCount = 1,
            mistreatmentIndicators = "Caquexia / Emaciação;;Privação de água potável",
            adequateIndicators = "Escore corporal adequado/ideal;;Água limpa disponível",
            requiresSeagriApprehension = true,
            seagriNotes = "Caminhão gaiola solicitado",
            requiresPmdfSupport = true,
            pmdfNotes = "Apoio BPMA para escolta",
            riskLevel = 2,
            riskJustification = "Área controlada sem tráfego de alta velocidade",
            safetyRiskAssessment = "Risco baixo de invasão de pista expressa",
            imageNotes = "Animal em boas condições de nutrição",
            tutorName = "Carlos Eduardo Lima",
            tutorCpf = "123.456.789-00"
        )

        val id = dao.insertInspection(inspection)
        val loaded = dao.getInspectionById(id).first()

        assertNotNull(loaded)
        assertEquals("DF-EQU-TEST-001", loaded?.protocolNumber)
        assertEquals(-15.7942, loaded?.latitude ?: 0.0, 0.0001)
        assertEquals(-47.8822, loaded?.longitude ?: 0.0, 0.0001)
        assertEquals("Cavalo manga-larga marchador, castanho, porte grande, sem ferimentos", loaded?.horseDescription)
        assertEquals(2, loaded?.horseCount)
        assertEquals(1, loaded?.mistreatedHorseCount)
        assertTrue(loaded?.hasMistreatmentSigns == true)
        assertEquals(2, loaded?.mistreatmentList?.size)
        assertEquals("Caquexia / Emaciação", loaded?.mistreatmentList?.get(0))
        assertEquals(2, loaded?.adequateList?.size)
        assertEquals("Escore corporal adequado/ideal", loaded?.adequateList?.get(0))
        assertTrue(loaded?.requiresSeagriApprehension == true)
        assertEquals("Caminhão gaiola solicitado", loaded?.seagriNotes)
        assertTrue(loaded?.requiresPmdfSupport == true)
        assertEquals("Apoio BPMA para escolta", loaded?.pmdfNotes)
        assertEquals(2, loaded?.riskLevel)
        assertEquals("Risco baixo de invasão de pista expressa", loaded?.safetyRiskAssessment)

        db.close()
    }
}
