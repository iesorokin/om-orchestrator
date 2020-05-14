package ru.iesorokin.orchestrator.input.stream.receiver

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.support.MessageBuilder
import ru.iesorokin.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.orchestrator.config.MessagingSource
import ru.iesorokin.orchestrator.core.service.BillingService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.CorrelationMessage

class BillingReceiverTest : BaseSpringBootWithCamundaTest() {
    @Autowired
    private lateinit var messagingSource: MessagingSource
    @MockBean
    private lateinit var billingService: BillingService

    val paymentTaskId = "12432"
    val billingApp = "BILLING-INTERACTION"

    @Test
    fun `receiveBillingMessage successfully`() {
        val message = MessageBuilder.withPayload(jsonMessage).build()
        messagingSource.billingPaidStatusInput().send(message)

        val payload = CorrelationMessage(billingApp, paymentTaskId)

        verify(billingService).processBillingPaidStatusMessage(payload)
    }

    @Test
    fun `receiveBillingMessage should not do anything if count is greater than maxRetry`() {
        val message = MessageBuilder
                .withPayload(jsonMessage)
                .setHeader(X_DEATH_HEADER, mapOf("count" to 11.toLong()))
                .build()
        messagingSource.billingPaidStatusInput().send(message)

        verifyZeroInteractions(billingService)
    }

    val jsonMessage = """
            {
                "sender": "$billingApp",
                "correlationKey": $paymentTaskId
            }
        """.trimIndent()
}
