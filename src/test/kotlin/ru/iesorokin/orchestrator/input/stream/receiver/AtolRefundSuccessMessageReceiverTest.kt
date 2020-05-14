package ru.iesorokin.payment.orchestrator.input.stream.receiver

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.support.MessageBuilder
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.config.MessagingSource
import ru.iesorokin.payment.orchestrator.core.service.refund.AtolRefundSuccessMessageService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.AtolTransactionFiscalData
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.AtolTransactionMessage

class AtolRefundSuccessMessageReceiverTest : BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var messagingSource: MessagingSource

    @MockBean
    private lateinit var service: AtolRefundSuccessMessageService

    @get:Rule
    val thrown = ExpectedException.none()!!

    @Test
    fun `should receive message and start process`() {

        @Language("JSON")
        var json = """
            {
                "uuid": "uuid",
                "ecrRegistrationNumber": "regNumber",
                "fiscalDocumentNumber": 1234,
                "fiscalStorageNumber": "fiscalNumber"

            }
        """.trimIndent()

        @Language("JSON")
        json = """
            {
                "atolId": "atolId",
                "status": "status",
                "fiscalData": $json
            }
        """.trimIndent()

        val message = MessageBuilder.withPayload(json)
                .build()
        messagingSource.atolTransactionStatusModifiedRefundInput().send(message)

        verify(service).processMessage(
                AtolTransactionMessage(
                        "atolId",
                        "status",
                        AtolTransactionFiscalData(
                                "uuid",
                                "regNumber",
                                1234,
                                "fiscalNumber"
                        )
                )
        )
    }

    @Test
    fun `should not do anything if count is greater or equals maxRetry`() {
        @Language("JSON")
        var json = """
            {
                "uuid": "uuid",
                "ecrRegistrationNumber": "regNumber",
                "fiscalDocumentNumber": 1234,
                "fiscalStorageNumber": "fiscalNumber"

            }
        """.trimIndent()

        @Language("JSON")
        json = """
            {
                "atolId": "atolId",
                "status": "status",
                "fiscalData": $json
            }
        """.trimIndent()

        val message = MessageBuilder.withPayload(json)
                .setHeader(X_DEATH_HEADER, mapOf("count" to 10.toLong()))
                .build()

        messagingSource.atolTransactionStatusModifiedRefundInput().send(message)

        MessageBuilder.withPayload(json)
                .setHeader(X_DEATH_HEADER, mapOf("count" to 11.toLong()))
                .build()

        messagingSource.atolTransactionStatusModifiedRefundInput().send(message)

        verifyZeroInteractions(service)
    }
}
