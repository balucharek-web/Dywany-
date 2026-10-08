package com.example

import com.example.data.model.Carpet
import com.example.data.util.LeroyMerlinParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun carpetDiscountCalculation_isCorrect() {
        val carpet = Carpet(
            name = "Dywan Testowy",
            ean = "5901234567890",
            lmCode = "82345001",
            widthCm = 160,
            lengthCm = 230,
            material = "Wełna",
            patternStyle = "Klasyczny",
            regularPrice = 1000.0,
            discountPrice = 800.0,
            warehouseContainer = "Kontener 1",
            warehouseSlot = "A"
        )

        assertTrue(carpet.hasDiscount)
        assertEquals(800.0, carpet.currentPrice, 0.001)
        assertEquals(20, carpet.discountPercentage)
        assertEquals("160 × 230 cm", carpet.dimensionsFormatted)
        assertEquals("Kontener 1 • Miejsce A", carpet.warehouseLocationFormatted)
        assertEquals("K1-A", carpet.locationShortBadge)
        assertTrue(carpet.isInWarehouse)
        assertFalse(carpet.isOnStand)
    }

    @Test
    fun leroyMerlinParser_validatesCodesAndSearches() {
        assertTrue(LeroyMerlinParser.isValidLmCode("82345001"))
        assertFalse(LeroyMerlinParser.isValidLmCode("123"))
        assertTrue(LeroyMerlinParser.isValidEan("5901234110012"))

        // Search by keyword
        val agnellaResults = LeroyMerlinParser.searchProducts("Agnella")
        assertTrue(agnellaResults.isNotEmpty())
        assertTrue(agnellaResults.any { it.title.contains("Agnella", ignoreCase = true) })

        // Search by 8-digit LM code
        val codeResults = LeroyMerlinParser.searchProducts("82345003")
        assertTrue(codeResults.isNotEmpty())
        assertEquals("82345003", codeResults.first().lmCode)

        // Search by Leroy Merlin URL
        val urlResults = LeroyMerlinParser.searchProducts("https://www.leroymerlin.pl/dywany/dywan-rabbit-82345003.html")
        assertTrue(urlResults.isNotEmpty())
        assertEquals("82345003", urlResults.first().lmCode)
    }
}
