package ru.iesorokin.payment.orchestrator.output.client.payment.sberbank

import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.zalando.logbook.Logbook
import ru.iesorokin.payment.orchestrator.config.RestTemplateConfig
import ru.iesorokin.payment.orchestrator.core.constants.process.PERSON
import ru.iesorokin.payment.orchestrator.output.client.payment.SberbankClient
import kotlin.test.assertTrue

private const val DELIVERY_MODE_COURIER = "COURIER"
private const val AMOUNT_1000 = 1000
private const val CUSTOMER_TYPE_PERSON = PERSON
private const val CUSTOMER_RELIABILITY_0 = 0
private const val CUSTOMER_AUTH_TRUE = true

class SberbankClientTest {
    private lateinit var sberbankClient: SberbankClient
    private val endpointTransactionRefund = "/transaction-refund/{orderId}"
    private val endpointAvailability = "/availability/"

    @Rule
    @JvmField
    var wireMock = WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort().portNumber())

    @Before
    fun setUp() {
        val urlTransactionRefund = "http://localhost:${wireMock.port()}$endpointTransactionRefund"
        val urlAvailability = "http://localhost:${wireMock.port()}$endpointAvailability"
        val restTemplate = RestTemplateConfig(Logbook.create(), 10, 10)
                .restTemplateSberbank(RestTemplateBuilder(), 5000, 5000)

        sberbankClient = SberbankClient(
                sberbankRestTemplate = restTemplate,
                urlTransactionRefund = urlTransactionRefund,
                availabilityUrl = urlAvailability
        )
    }

    @Test
    fun `refund should send request`() {
        val orderId = "orderId"
        val storeId = 10
        val refundAmount = "200.111".toBigDecimal()

        stubFor(post(endpointTransactionRefund.replace("{orderId}", orderId))
                .withRequestBody(equalToJson("""{
                    "refundAmount": 200.11,
                    "storeId": 10
                }""".trimIndent()))
                .willReturn(ok()))

        sberbankClient.refund(orderId, storeId, refundAmount)

        verify(postRequestedFor(urlEqualTo(endpointTransactionRefund.replace("{orderId}", orderId)))
                .withRequestBody(equalToJson("""{
                    "refundAmount": 200.11,
                    "storeId": 10
                }""".trimIndent())))
    }

    @Test
    fun `isPaymentClientAvailable should return true`() {
        val paymentClientAvailable = sberbankClient.isPaymentClientAvailable(DELIVERY_MODE_COURIER,
                CUSTOMER_AUTH_TRUE, CUSTOMER_TYPE_PERSON, CUSTOMER_RELIABILITY_0, AMOUNT_1000.toBigDecimal())

        assertTrue(paymentClientAvailable)
    }
}
