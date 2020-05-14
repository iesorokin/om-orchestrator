package ru.iesorokin.orchestrator.core.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.iesorokin.opus.client.artmag.ArtmagOpusClient
import ru.iesorokin.opus.client.artmag.dto.ArtMag
import ru.iesorokin.opus.client.product.ProductOpusClient
import ru.iesorokin.opus.client.product.dto.ProductAttribute

class OpusServiceTest {
    private val productOpusClient = mock<ProductOpusClient>()
    private val artmagOpusClient = mock< ArtmagOpusClient>()
    private val opusService = OpusService(productOpusClient, artmagOpusClient)

    @Test
    fun `getProductNameByProductId should return product name by productId`() {
        val productIds = setOf("13064724", "13064725")
        val attributes = setOf("12963", "customerShortLabel")
        val russian = "RU"
        val version = "v2"
        val channel = "Payment"
        val showDescription = false
        val expected = mapOf(
                "13064724" to "Отвертка",
                "13064725" to "Молоток"
        )
        whenever(productOpusClient.getProductsAttributes(productIds, attributes, russian, showDescription, version, channel))
                .thenReturn(mapOf(
                        "13064724" to mapOf(
                                "12963" to ProductAttribute().apply { value = listOf("Отвертка") }
                        ),
                        "13064725" to mapOf(
                                "customerShortLabel" to ProductAttribute().apply { value = listOf("Молоток") }
                        )
                ))

        val actual = opusService.getProductNameByProductId(productIds)

        assertThat(expected).isEqualTo(actual)
    }

    @Test
    fun `getVatByProductId should return vat by productId`() {
        val productIds = setOf("13064724", "13064725")
        val store = "49"
        val expected = mapOf(
                "13064724" to 15.toBigDecimal(),
                "13064725" to 20.toBigDecimal()
        )
        whenever(artmagOpusClient.getArtmagByStores(productIds, setOf(store)))
                .thenReturn(mapOf(
                        "13064724" to mapOf(
                                "49" to ArtMag().apply { vatRateValue = 15.toBigDecimal() }
                        ),
                        "13064725" to mapOf(
                                "49" to ArtMag().apply { vatRateValue = 20.toBigDecimal() }
                        )
                ))

        val actual = opusService.getVatByProductId(productIds, store)

        assertThat(expected).isEqualTo(actual)
    }
}