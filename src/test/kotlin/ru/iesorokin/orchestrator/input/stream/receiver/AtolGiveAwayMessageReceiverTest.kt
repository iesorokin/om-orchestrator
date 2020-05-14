package ru.iesorokin.payment.orchestrator.input.stream.receiver

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.support.MessageBuilder
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.config.MessagingSource
import ru.iesorokin.payment.orchestrator.core.service.giveaway.AtolGiveAwayMessageService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.AtolTransactionFiscalData
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.AtolTransactionMessage

class AtolGiveAwayMessageReceiverTest : BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var messagingSource: MessagingSource

    @MockBean
    private lateinit var service: AtolGiveAwayMessageService

    @get:Rule
    val thrown = ExpectedException.none()!!

    @Test
    fun `should receive message and process it`() {
        val message = MessageBuilder.withPayload(messageJson()).build()

        messagingSource.atolTransactionStatusModifiedGiveAwayInput().send(message)

        verify(service).processMessage(
                AtolTransactionMessage(
                        atolId = "atolId",
                        status = "status",
                        processInstanceId = "processInstanceId",
                        fiscalData = AtolTransactionFiscalData(
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
        val message = MessageBuilder.withPayload(messageJson())
                .setHeader(X_DEATH_HEADER, mapOf("count" to 10.toLong()))
                .build()

        messagingSource.atolTransactionStatusModifiedGiveAwayInput().send(message)

        MessageBuilder.withPayload(messageJson())
                .setHeader(X_DEATH_HEADER, mapOf("count" to 11.toLong()))
                .build()

        messagingSource.atolTransactionStatusModifiedGiveAwayInput().send(message)

        verifyZeroInteractions(service)
    }

    private fun messageJson() = """
            {
                "atolId": "atolId",
                "status": "status",
                "processInstanceId": "processInstanceId",
                "fiscalData": {
                    "uuid": "uuid",
                    "ecrRegistrationNumber": "regNumber",
                    "fiscalDocumentNumber": 1234,
                    "fiscalStorageNumber": "fiscalNumber"
                }
            }
        """.trimIndent()
}
