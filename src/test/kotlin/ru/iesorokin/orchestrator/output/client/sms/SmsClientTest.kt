package ru.iesorokin.payment.orchestrator.output.client.sms

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.web.client.RestTemplate
import org.zalando.logbook.Logbook
import ru.iesorokin.payment.orchestrator.config.LEAD_APPLICATION
import ru.iesorokin.payment.orchestrator.config.RestTemplateConfig
import ru.iesorokin.payment.orchestrator.core.domain.InternalSmsRequest
import ru.iesorokin.payment.orchestrator.core.service.NOW
import ru.iesorokin.payment.orchestrator.core.service.REFUND_AMOUNT
import ru.iesorokin.payment.orchestrator.core.service.SOLUTION_ID
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SmsClientTest {
    private lateinit var smsClient: SmsClient
    private val urlSendMultiSms = "/v3/sms/send-multi"
    private lateinit var restTemplate: RestTemplate

    @Rule
    @JvmField
    var wireMock = WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort().portNumber())

    @Before
    fun setUp() {
        val urlSendMultiSms = "http://localhost:${wireMock.port()}$urlSendMultiSms"
        val restTemplateBuilder = RestTemplateBuilder()
        restTemplate = RestTemplateConfig(Logbook.create(), 100, 100)
                .restTemplateSms(restTemplateBuilder, 1000, 1000)
        smsClient = SmsClient(restTemplate, urlSendMultiSms, true, SmsConverter())
    }

    @Test
    fun `should send multi sms`() {
        WireMock.stubFor(WireMock.post(urlSendMultiSms)
                .withHeader(ACCEPT, equalTo("application/xml"))
                .withHeader(LEAD_APPLICATION, equalTo("PUZ2"))
                .withRequestBody(equalToJson(postSendMultiSmsBody))
                .willReturn(WireMock.okJson(postSendMultiSms200Response)))

        val phone = "+79999999999"
        val customerName = "Andy"
        val event = "refundForPrepayment"
        val appSender = "puz2"

        smsClient.sendMultiSms(listOf(InternalSmsRequest(

                event,
                phone,
                NOW,
                123,
                mapOf("customerName" to customerName,
                        "solutionId" to SOLUTION_ID,
                        REFUND_AMOUNT to "25.15"),
                appSender
        )))
        WireMock.verify(1, postRequestedFor(urlEqualTo(urlSendMultiSms)))
    }

    @Test
    fun `shouldn't send multi sms when sendSms is false`() {
        smsClient = SmsClient(restTemplate, urlSendMultiSms, false, SmsConverter())

        val phone = "+79999999999"
        val customerName = "Andy"
        val event = "refundForPrepayment"
        val appSender = "puz2"

        smsClient.sendMultiSms(listOf(InternalSmsRequest(

                event,
                phone,
                NOW,
                123,
                mapOf("customerName" to customerName,
                        "solutionId" to SOLUTION_ID,
                        REFUND_AMOUNT to "25.15"),
                appSender
        )))
        WireMock.verify(0, postRequestedFor(urlEqualTo(urlSendMultiSms)))
    }

    @Language("JSON")
    val postSendMultiSmsBody = """
        [{
          "event": "refundForPrepayment",
          "receiver": "+79999999999",
          "sendingType": "NOW",
          "storeId": 123,
          "templateData": {
            "customerName": "Andy",
            "solutionId": "solutionId",
            "refundAmount": "25.15"
          },
          "templateOwner": "puz2"
        }]
    """.trimIndent()

    @Language("JSON")
    val postSendMultiSms200Response = """
        [
          {
            "id": "5cd03e01b74f230001137087",
            "receiver": "+79999999999",
            "message": "Здравствуйте, Andy! По Вашему заказу solutionId осуществлён возврат 25.15 руб. Срок зачисления от 3 до 14 дней в зависимости от условий Вашего банка. Подробнее: 8 (800) 700-00-99",
            "status": "SENT"
          }
        ]
    """.trimIndent()


}
