package com.example

import com.example.model.HorseAnatomyData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testHorseAnatomyDataRegionsAndCrops() {
        assertTrue(HorseAnatomyData.regions.contains("Todas as Regiões"))
        assertTrue(HorseAnatomyData.regions.contains("Cabeça"))
        assertTrue(HorseAnatomyData.regions.contains("Casco"))
        assertTrue(HorseAnatomyData.regions.contains("Membros Inferiores"))
        assertTrue(HorseAnatomyData.regions.contains("Membros Anteriores"))
        assertTrue(HorseAnatomyData.regions.contains("Membros Posteriores"))
        assertTrue(HorseAnatomyData.regions.contains("Tronco & Dorso"))
        assertTrue(HorseAnatomyData.regions.contains("Pescoço"))

        // Verifica corte de cabeça
        val cabecaCrop = HorseAnatomyData.getCropForRegion("Cabeça")
        assertNotNull(cabecaCrop)
        assertEquals(R.drawable.anatomia_corte_cabeca, cabecaCrop.drawableResId)

        // Verifica corte de casco
        val cascoCrop = HorseAnatomyData.getCropForRegion("Casco")
        assertNotNull(cascoCrop)
        assertEquals(R.drawable.anatomia_corte_casco, cascoCrop.drawableResId)

        // Verifica corte de membros inferiores
        val membrosInfCrop = HorseAnatomyData.getCropForRegion("Membros Inferiores")
        assertNotNull(membrosInfCrop)
        assertEquals(R.drawable.anatomia_corte_membros_inferiores, membrosInfCrop.drawableResId)

        // Verifica corte de membros anteriores
        val membrosAntCrop = HorseAnatomyData.getCropForRegion("Membros Anteriores")
        assertNotNull(membrosAntCrop)
        assertEquals(R.drawable.anatomia_corte_membros_anteriores, membrosAntCrop.drawableResId)

        // Verifica corte de membros posteriores
        val membrosPostCrop = HorseAnatomyData.getCropForRegion("Membros Posteriores")
        assertNotNull(membrosPostCrop)
        assertEquals(R.drawable.anatomia_corte_membros_posteriores, membrosPostCrop.drawableResId)

        // Verifica corte de tronco & dorso
        val troncoCrop = HorseAnatomyData.getCropForRegion("Tronco & Dorso")
        assertNotNull(troncoCrop)
        assertEquals(R.drawable.anatomia_corte_tronco, troncoCrop.drawableResId)

        // Verifica corte de pescoço
        val pescocoCrop = HorseAnatomyData.getCropForRegion("Pescoço")
        assertNotNull(pescocoCrop)
        assertEquals(R.drawable.anatomia_corte_pescoco, pescocoCrop.drawableResId)

        // Verifica visão geral / todas as regiões
        val todasCrop = HorseAnatomyData.getCropForRegion("Todas as Regiões")
        assertNotNull(todasCrop)
        assertEquals(R.drawable.anatomia_cavalo_atlas, todasCrop.drawableResId)
    }

    @Test
    fun testHorseAnatomyDataPointsFiltering() {
        val allPoints = HorseAnatomyData.getPointsForRegion("Todas as Regiões")
        assertTrue(allPoints.size >= 15)

        val cabecaPoints = HorseAnatomyData.getPointsForRegion("Cabeça")
        assertTrue(cabecaPoints.isNotEmpty())
        assertTrue(cabecaPoints.all { it.region == "Cabeça" })
        assertTrue(cabecaPoints.any { it.id == "chanfro" })
        assertTrue(cabecaPoints.any { it.id == "fronte" })

        val cascoPoints = HorseAnatomyData.getPointsForRegion("Casco")
        assertTrue(cascoPoints.isNotEmpty())
        assertTrue(cascoPoints.any { it.id == "muralha_casco" })
        assertTrue(cascoPoints.any { it.id == "sola_ranilha" })

        val membrosInfPoints = HorseAnatomyData.getPointsForRegion("Membros Inferiores")
        assertTrue(membrosInfPoints.isNotEmpty())
        assertTrue(membrosInfPoints.any { it.id == "canela_ant" || it.id == "boleto_ant" })
    }
}
