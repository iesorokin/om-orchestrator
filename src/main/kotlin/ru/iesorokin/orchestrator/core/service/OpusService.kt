package ru.iesorokin.payment.orchestrator.core.service

import org.springframework.stereotype.Service
import ru.iesorokin.opus.client.artmag.ArtmagOpusClient
import ru.iesorokin.opus.client.product.ProductOpusClient
import ru.iesorokin.opus.client.product.dto.ProductAttribute
import ru.iesorokin.payment.orchestrator.core.enums.Language
import ru.iesorokin.payment.orchestrator.core.enums.OpusAttribute
import java.math.BigDecimal

private const val VERSION_2 = "v2"
private const val CHANNEL = "Payment"

@Service
class OpusService(
        private val productOpusClient: ProductOpusClient,
        private val artmagOpusClient: ArtmagOpusClient
) {

    fun getProductNameByProductId(productIds: Set<String>) : Map<String, String> {
        val attributes = setOf(OpusAttribute.DISPLAYED_NAME.attribute, OpusAttribute.SHORT_DISPLAYED_NAME.attribute)
        val productsAttributes = productOpusClient
                .getProductsAttributes(productIds, attributes, Language.RUSSIAN.shortCode, false, VERSION_2, CHANNEL)
        return productsAttributes.map { it.key to getProductName(it.value) }.toMap()
    }

    private fun getProductName(productAttributes: Map<String, ProductAttribute>) =
            productAttributes[OpusAttribute.DISPLAYED_NAME.attribute]?.value?.firstOrNull() ?:
            productAttributes[OpusAttribute.SHORT_DISPLAYED_NAME.attribute]?.value?.first()!!

    fun getVatByProductId(productIds: Set<String>, store: String): Map<String, BigDecimal> {
        val artmagByStores = artmagOpusClient.getArtmagByStores(productIds, setOf(store))
        return artmagByStores.map { it.key to it.value[store]!!.vatRateValue }.toMap()
    }
}