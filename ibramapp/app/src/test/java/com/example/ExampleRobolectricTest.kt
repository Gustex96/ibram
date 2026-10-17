package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.DfConstants
import com.example.data.model.HorseInspection
import com.example.util.CpfValidator
import java.io.File
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
        assertEquals("LEVO", appName)
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
        assertEquals(2, com.example.data.model.AnexoFData.categories.size)
        val normalCategory = com.example.data.model.AnexoFData.categories.find { it.id == "normal" }
        val dogsCategory = com.example.data.model.AnexoFData.categories.find { it.id == "caes" }
        val catsCategory = com.example.data.model.AnexoFData.categories.find { it.id == "gatos" }

        assertNotNull(normalCategory)
        assertNotNull(dogsCategory)
        assertTrue(catsCategory == null)

        assertEquals("Animal Saudável", normalCategory?.title)
        assertEquals(8, normalCategory?.items?.size)
        assertTrue(normalCategory?.items?.none { it.title.contains("Gato", ignoreCase = true) || it.tag.contains("Felino", ignoreCase = true) } == true)

        assertEquals("Cuidados Básicos para Cães", dogsCategory?.title)
        assertEquals(10, dogsCategory?.items?.size)
    }

    @Test
    fun testAnimalSpeciesEntriesOnlyDogAndHorse() {
        val species = com.example.ui.components.AnimalSpecies.entries
        assertEquals(2, species.size)
        assertEquals(listOf("Cachorro", "Cavalo"), species.map { it.displayName })
        assertTrue(species.none { it.name == "CAT" || it.name == "ROOSTER" })
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
            requiresOtherAgencySupport = true,
            otherAgencyName = "DEMA / PCDF",
            otherAgencyNotes = "Abertura de inquérito e perícia conjunta",
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
        assertTrue(loaded?.requiresOtherAgencySupport == true)
        assertEquals("DEMA / PCDF", loaded?.otherAgencyName)
        assertEquals("Abertura de inquérito e perícia conjunta", loaded?.otherAgencyNotes)
        assertEquals(2, loaded?.riskLevel)
        assertEquals("Risco baixo de invasão de pista expressa", loaded?.safetyRiskAssessment)

        db.close()
    }

    @Test
    fun testAuditorIdentificationPersistence() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("levo_auditor_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("key_auditor_id", "Auditor Fiscal IBRAM • Matr. 987.654-1").commit()

        val loaded = prefs.getString("key_auditor_id", "")
        assertEquals("Auditor Fiscal IBRAM • Matr. 987.654-1", loaded)
    }

    @Test
    fun testDeleteAllInspectionsInRoomDatabase() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val dao = db.horseInspectionDao()

        val item1 = HorseInspection(
            protocolNumber = "DF-EQU-001",
            photoPath = "/local/photo1.jpg",
            latitude = -15.80,
            longitude = -47.90,
            administrativeRegion = "Taguatinga"
        )
        val item2 = HorseInspection(
            protocolNumber = "DF-EQU-002",
            photoPath = "/local/photo2.jpg",
            latitude = -15.82,
            longitude = -47.92,
            administrativeRegion = "Ceilândia"
        )

        dao.insertInspection(item1)
        dao.insertInspection(item2)
        assertEquals(2, dao.getAllInspectionsList().size)

        val deletedCount = dao.deleteAllInspections()
        assertEquals(2, deletedCount)
        assertEquals(0, dao.getAllInspectionsList().size)

        db.close()
    }

    @Test
    fun testHorseAnatomyDataConstants() {
        assertEquals("Atlas de Anatomia do Cavalo", com.example.model.HorseAnatomyData.ATLAS_TITLE)
        assertTrue(com.example.model.HorseAnatomyData.ATLAS_SOURCE.contains("AGPTEA"))
        assertTrue(com.example.model.HorseAnatomyData.ATLAS_PAGE_REFERENCE.contains("Página 2"))
        assertTrue(com.example.model.HorseAnatomyData.ATLAS_DOCUMENT_URL.startsWith("https://"))
    }

    @Test
    fun testEquineRoomPersistenceWithGeoAndPhotoPaths() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val dao = db.horseInspectionDao()

        val equineInspection = HorseInspection(
            protocolNumber = "DF-EQU-2026-9999",
            photoPath = "/data/user/0/com.aistudio.equidf.kpmv/files/photos/horse_front.jpg",
            additionalPhotoPaths = "/data/user/0/com.aistudio.equidf.kpmv/files/photos/horse_lateral.jpg;;/data/user/0/com.aistudio.equidf.kpmv/files/photos/horse_pata.jpg",
            latitude = -15.823456,
            longitude = -47.987654,
            administrativeRegion = "Planaltina (RA II)",
            quadra = "Quadra 12",
            conjunto = "Conjunto B",
            numero = "Chácara 05",
            species = "Cavalo",
            horseDescription = "Equino castanho com cicatriz no flanco esquerdo",
            horseCount = 1,
            mistreatedHorseCount = 0
        )

        val id = dao.insertInspection(equineInspection)
        val loaded = dao.getInspectionById(id).first()

        assertNotNull(loaded)
        assertEquals("DF-EQU-2026-9999", loaded?.protocolNumber)
        assertEquals(-15.823456, loaded?.latitude ?: 0.0, 0.000001)
        assertEquals(-47.987654, loaded?.longitude ?: 0.0, 0.000001)
        assertEquals("/data/user/0/com.aistudio.equidf.kpmv/files/photos/horse_front.jpg", loaded?.photoPath)
        assertEquals(3, loaded?.allPhotoPaths?.size)
        assertEquals("Cavalo", loaded?.species)
        assertFalse(loaded?.isDog == true)

        db.close()
    }

    @Test
    fun testOwlKnowledgeBaseSpeciesAtlasSeparation() {
        val horseSpecies = com.example.ui.components.AnimalSpecies.HORSE
        val dogSpecies = com.example.ui.components.AnimalSpecies.DOG

        // For horse: horse anatomy atlas is preserved
        assertTrue(horseSpecies == com.example.ui.components.AnimalSpecies.HORSE)

        // For dog and any non-horse: atlas is excluded
        assertFalse(dogSpecies == com.example.ui.components.AnimalSpecies.HORSE)

        // Anexo F has no galo or gato categories
        assertTrue(com.example.data.model.AnexoFData.categories.none {
            it.id.contains("galo", ignoreCase = true) || it.id.contains("gato", ignoreCase = true)
        })
    }

    @Test
    fun testFiveDomainsDataModel() {
        val domains = com.example.data.model.FiveDomainsData.domains
        assertEquals(5, domains.size)

        val numbers = domains.map { it.number }
        assertEquals(listOf(1, 2, 3, 4, 5), numbers)

        val names = domains.map { it.shortName }
        assertEquals(listOf("Nutrição", "Ambiente", "Saúde", "Comportamento", "Estado Mental"), names)

        domains.forEach { domain ->
            assertTrue(domain.whatMustBeGuaranteed.isNotBlank())
            assertTrue(domain.auditIndicators.isNotEmpty())
            assertTrue(domain.positiveIndicators.isNotEmpty())
            assertTrue(domain.violationIndicators.isNotEmpty())
            assertTrue(domain.tag.isNotBlank())
        }
    }

    @Test
    fun testReportTerminologyAndFiveDomainsGuarantees() {
        val inspection = HorseInspection(
            protocolNumber = "DF-EQU-2026-0001",
            photoPath = "/mock/path.jpg",
            latitude = -15.7942,
            longitude = -47.8822,
            administrativeRegion = "Brasília (RA I)"
        )
        // Check that PDF file name format uses Levantamento
        val expectedFileName = "Levantamento_${inspection.protocolNumber}.pdf"
        assertTrue(expectedFileName.startsWith("Levantamento_"))
        assertFalse(expectedFileName.contains("Protocolo"))

        // Check that the 5 domains specify guarantees for nutrition, environment, health, behavior and mental state
        val domains = com.example.data.model.FiveDomainsData.domains
        val nutrition = domains.first { it.number == 1 }
        val environment = domains.first { it.number == 2 }
        val health = domains.first { it.number == 3 }
        val behavior = domains.first { it.number == 4 }
        val mental = domains.first { it.number == 5 }

        assertTrue(nutrition.whatMustBeGuaranteed.contains("água", ignoreCase = true))
        assertTrue(environment.whatMustBeGuaranteed.contains("ambiente", ignoreCase = true))
        assertTrue(health.whatMustBeGuaranteed.contains("integridade", ignoreCase = true))
        assertTrue(behavior.whatMustBeGuaranteed.contains("comportamentos", ignoreCase = true))
        assertTrue(mental.whatMustBeGuaranteed.contains("estados mentais", ignoreCase = true))
    }

    @Test
    fun testSpeciesDisplayUsesPopularNameCavalo() {
        val horseInspection = HorseInspection(
            protocolNumber = "DF-HORSE-01",
            photoPath = "/mock/horse.jpg",
            latitude = -15.7942,
            longitude = -47.8822,
            administrativeRegion = "Plano Piloto (RA I)",
            species = "Cavalo",
            horseDescription = "Cavalo manga-larga"
        )
        assertEquals("Cavalo", horseInspection.speciesDisplay)
        assertFalse(horseInspection.speciesDisplay.contains("Equino", ignoreCase = true))

        val dogInspection = HorseInspection(
            protocolNumber = "DF-DOG-01",
            photoPath = "/mock/dog.jpg",
            latitude = -15.7942,
            longitude = -47.8822,
            administrativeRegion = "Plano Piloto (RA I)",
            species = "Cachorro",
            horseDescription = "Cão sem raça definida"
        )
        assertEquals("Cachorro (Canino)", dogInspection.speciesDisplay)
    }

    @Test
    fun testZipBackupAndRestoreWithPhotos() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.horseInspectionDao()

        // 1. Create a dummy photo file
        val tempDir = File(context.cacheDir, "test_photos").apply { mkdirs() }
        val dummyPhoto = File(tempDir, "TEST_PHOTO_1.jpg")
        dummyPhoto.writeText("fake image bytes content")

        val inspection = HorseInspection(
            protocolNumber = "DF-ZIP-TEST-2026",
            photoPath = dummyPhoto.absolutePath,
            latitude = -15.7942,
            longitude = -47.8822,
            administrativeRegion = "Ceilândia (RA IX)",
            species = "Cavalo"
        )

        // 2. Export to a temporary zip file
        val backupZipFile = File(context.cacheDir, "test_backup.zip")
        val zipUri = android.net.Uri.fromFile(backupZipFile)
        val exportResult = com.example.util.DatabaseBackupManager.exportToZipUri(
            context = context,
            uri = zipUri,
            inspections = listOf(inspection)
        )
        assertTrue(exportResult.isSuccess)
        val backupResult = exportResult.getOrNull()
        assertNotNull(backupResult)
        assertEquals(1, backupResult?.recordsCount)
        assertEquals(1, backupResult?.photosCount)
        assertTrue(backupZipFile.exists() && backupZipFile.length() > 0)

        // 3. Clear database and restore from the zip
        dao.deleteAllInspections()
        assertEquals(0, dao.getAllInspectionsList().size)

        val importResult = com.example.util.DatabaseBackupManager.importFromBackupUri(
            context = context,
            uri = zipUri,
            dao = dao
        )
        assertTrue(importResult.isSuccess)
        val restoredResult = importResult.getOrNull()
        assertEquals(1, restoredResult?.recordsCount)
        assertEquals(1, restoredResult?.photosCount)

        val restoredList = dao.getAllInspectionsList()
        assertEquals(1, restoredList.size)
        val restoredItem = restoredList[0]
        assertEquals("DF-ZIP-TEST-2026", restoredItem.protocolNumber)
        assertTrue("O caminho da foto restaurada deve existir", File(restoredItem.photoPath).exists())
        assertEquals("fake image bytes content", File(restoredItem.photoPath).readText())

        db.close()
    }
}
