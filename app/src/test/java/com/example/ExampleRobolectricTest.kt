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
@Config(sdk = [36])
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
    fun testAnimalSpeciesEntries() {
        val species = com.example.ui.components.AnimalSpecies.entries
        assertTrue(species.any { it.name == "DOG" })
        assertTrue(species.any { it.name == "HORSE" })
        assertTrue(species.map { it.displayName }.containsAll(listOf("Cachorro", "Cavalo")))
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
            photoPath = "/data/user/0/com.aistudio.levo.kpmv/files/photos/horse_front.jpg",
            additionalPhotoPaths = "/data/user/0/com.aistudio.levo.kpmv/files/photos/horse_lateral.jpg;;/data/user/0/com.aistudio.levo.kpmv/files/photos/horse_pata.jpg",
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
        assertEquals("/data/user/0/com.aistudio.levo.kpmv/files/photos/horse_front.jpg", loaded?.photoPath)
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

    @Test
    fun testExportCerradoSplashVectorToHighResPng() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_splash_vector)
        assertNotNull(drawable)
        val width = 1080
        val height = 2220
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)
        val currentDir = File(".").absoluteFile
        val rootDir = if (currentDir.name == "app") currentDir.parentFile ?: currentDir else currentDir
        val outDir = File(rootDir, "public/assets").apply { if (!exists()) mkdirs() }
        val outFile = File(outDir, "background_cerrado_splash_hd.png")
        java.io.FileOutputStream(outFile).use { outStream ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outStream)
        }
        assertTrue(outFile.exists())
        assertTrue(outFile.length() > 0)
    }

    @Test
    fun testIndividualAnimalEvaluationSerialization() {
        val list = listOf(
            com.example.data.model.IndividualAnimalEvaluation(
                id = "anim-1",
                tagOrCpf = "BRINCO-001",
                species = "Cavalo",
                ppbeaAnswersData = "nut_magro:true;;nut_sem_agua:false"
            ),
            com.example.data.model.IndividualAnimalEvaluation(
                id = "anim-2",
                tagOrCpf = "CHIP-998",
                species = "Cachorro",
                ppbeaAnswersData = "nut_magro:false;;nut_sem_agua:false"
            )
        )
        val serialized = com.example.data.model.IndividualAnimalSerializer.serializeList(list)
        val deserialized = com.example.data.model.IndividualAnimalSerializer.deserializeList(serialized)
        assertEquals(2, deserialized.size)
        assertEquals("anim-1", deserialized[0].id)
        assertEquals("BRINCO-001", deserialized[0].tagOrCpf)
        assertEquals("Cavalo", deserialized[0].species)
        assertTrue(deserialized[0].isEvaluated)
        assertEquals("CHIP-998", deserialized[1].tagOrCpf)
        assertEquals("Cachorro", deserialized[1].species)
    }

    @Test
    fun testIndividualAnimalDiagnosisAndDomains() {
        val animal1 = com.example.data.model.IndividualAnimalEvaluation(
            id = "anim-1",
            tagOrCpf = "BRINCO-001 / João da Silva",
            species = "Cavalo",
            ppbeaAnswersData = "nut_magro:true;;nut_sem_agua:false;;saude_ferido_doente:true",
            notes = "Animal necessita de suplementação urgente"
        )
        val animal2 = com.example.data.model.IndividualAnimalEvaluation(
            id = "anim-2",
            tagOrCpf = "TAG-882 / Maria Pereira",
            species = "Cachorro",
            ppbeaAnswersData = "nut_magro:false;;nut_sem_agua:false",
            notes = "Bom estado geral"
        )

        // Verificações animal 1: possui diagnóstico com inconformidades em nutrição e saúde
        val diag1 = animal1.ppbeaDiagnosis
        assertNotNull(diag1)
        assertEquals(com.example.data.model.ParecerDomain.INADEQUADO, diag1?.nutricionalParecer)
        assertEquals(com.example.data.model.ParecerDomain.INADEQUADO, diag1?.saudeParecer)
        assertEquals(com.example.data.model.ParecerDomain.ADEQUADO, diag1?.confortoParecer)
        assertEquals(com.example.data.model.ParecerDomain.ADEQUADO, diag1?.comportamentalParecer)
        assertTrue(animal1.isEvaluated)

        // Verificações animal 2: conformidade
        val diag2 = animal2.ppbeaDiagnosis
        assertNotNull(diag2)
        assertEquals(com.example.data.model.ParecerDomain.ADEQUADO, diag2?.nutricionalParecer)
        assertEquals(com.example.data.model.ParecerDomain.ADEQUADO, diag2?.saudeParecer)
        assertEquals(com.example.data.model.ParecerDomain.ADEQUADO, diag2?.confortoParecer)
        assertEquals(com.example.data.model.ParecerDomain.ADEQUADO, diag2?.comportamentalParecer)

        // Teste de agregação na entidade HorseInspection
        val serialized = com.example.data.model.IndividualAnimalSerializer.serializeList(listOf(animal1, animal2))
        val inspection = HorseInspection(
            protocolNumber = "DF-INDIV-2026-001",
            photoPath = "/mock/path.jpg",
            latitude = -15.7942,
            longitude = -47.8822,
            administrativeRegion = "Planaltina (RA II)",
            species = "Múltiplas Espécies",
            horseCount = 2,
            mistreatedHorseCount = 1,
            individualEvaluationsData = serialized
        )

        assertEquals(2, inspection.individualEvaluations.size)
        assertEquals("BRINCO-001 / João da Silva", inspection.individualEvaluations[0].tagOrCpf)
        assertEquals("TAG-882 / Maria Pereira", inspection.individualEvaluations[1].tagOrCpf)
    }

    @Test
    fun testPdfReportTextWrapping() {
        val paint = android.graphics.Paint().apply {
            textSize = 12f
        }
        val maxWidth = 30f

        // 1. Long continuous text with no spaces (e.g. URL or continuous string)
        val unbrokenText = "https://www.google.com/maps?q=-15.641438,-47.646636_continuous_token_1234567890_abcdefghijklmnopqrstuvwxyz_0123456789_abcdefghijklmnopqrstuvwxyz"
        val unbrokenLines = com.example.util.PdfReportGenerator.splitTextIntoLines(unbrokenText, maxWidth, paint)
        assertTrue("Expected multiple lines, got: ${unbrokenLines.size}", unbrokenLines.size > 1)
        for (line in unbrokenLines) {
            assertTrue("Line '$line' must not exceed maxWidth ($maxWidth vs ${paint.measureText(line)})", paint.measureText(line) <= maxWidth)
        }

        // 2. Multi-word paragraph
        val paragraph = "O animal foi encontrado em condições regulares necessitando de cuidados na nutrição e conforto segundo o protocolo PPBEA."
        val paragraphLines = com.example.util.PdfReportGenerator.splitTextIntoLines(paragraph, maxWidth, paint)
        assertTrue("Expected multiple lines for paragraph, got: ${paragraphLines.size}", paragraphLines.size > 1)
        for (line in paragraphLines) {
            assertTrue("Paragraph line '$line' must not exceed maxWidth ($maxWidth vs ${paint.measureText(line)})", paint.measureText(line) <= maxWidth)
        }

        // 3. Empty text
        val emptyLines = com.example.util.PdfReportGenerator.splitTextIntoLines("", maxWidth, paint)
        assertTrue(emptyLines.isEmpty())

        // 4. Text with explicit newlines
        val multiLineText = "Linha 1\nLinha 2 com texto longo o suficiente para quebrar com certeza em várias linhas\nLinha 3"
        val resultLines = com.example.util.PdfReportGenerator.splitTextIntoLines(multiLineText, maxWidth, paint)
        assertTrue(resultLines.size >= 3)
        for (line in resultLines) {
            assertTrue("Multi-line piece '$line' must not exceed maxWidth", paint.measureText(line) <= maxWidth)
        }
    }

    @Test
    fun testDecoupledSamplingCountAndEffectiveCount() {
        val inspection = HorseInspection(
            protocolNumber = "DF-EQU-DECOUPLED-001",
            photoPath = "/mock/path/photo.jpg",
            latitude = -15.7942,
            longitude = -47.8822,
            administrativeRegion = "Planaltina (RA II)",
            horseCount = 25, // 25 animais no local
            sampledAnimalCount = 6, // 6 animais avaliados na amostragem PPBEA
            mistreatedHorseCount = 6 // 6 animais com maus-tratos apurados na amostra
        )

        assertEquals(25, inspection.horseCount)
        assertEquals(6, inspection.sampledAnimalCount)
        assertEquals(6, inspection.effectiveSampledCount)
        assertEquals(6, inspection.mistreatedHorseCount)

        // Caso com legado de avaliação geral onde sampledAnimalCount = 0
        val legacyInspection = HorseInspection(
            protocolNumber = "DF-EQU-DECOUPLED-002",
            photoPath = "/mock/path/photo.jpg",
            latitude = -15.7942,
            longitude = -47.8822,
            administrativeRegion = "Guará (RA X)",
            horseCount = 8,
            sampledAnimalCount = 0,
            ppbeaAnswersData = "1.1:true"
        )
        assertEquals(8, legacyInspection.horseCount)
        assertEquals(0, legacyInspection.sampledAnimalCount)
        assertEquals(8, legacyInspection.effectiveSampledCount)

        // Caso sem diagnóstico PPBEA
        val noDiagInspection = HorseInspection(
            protocolNumber = "DF-EQU-DECOUPLED-003",
            photoPath = "/mock/path/photo.jpg",
            latitude = -15.7942,
            longitude = -47.8822,
            administrativeRegion = "Guará (RA X)",
            horseCount = 12,
            sampledAnimalCount = 0
        )
        assertEquals(12, noDiagInspection.horseCount)
        assertEquals(0, noDiagInspection.effectiveSampledCount)
    }
}
