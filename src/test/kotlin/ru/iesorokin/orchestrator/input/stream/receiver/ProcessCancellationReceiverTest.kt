package ru.iesorokin.payment.orchestrator.input.stream.receiver

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.support.MessageBuilder
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.config.MessagingSource
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.payment.orchestrator.core.service.prepayment.ProcessCancellationService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.CancelProcessMessage

class ProcessCancellationReceiverTest : BaseSpringBootWithCamundaTest() {
    @Autowired
    private lateinit var messagingSource: MessagingSource
    @MockBean
    private lateinit var processCancellationService: ProcessCancellationService

    val extOrderId = "1234"
    val paymentTaskId = "7334434"

    @Test
    fun `receiveMessage successfully`() {
        val message = MessageBuilder.withPayload(jsonMessage).build()
        messagingSource.cancelPrepaymentProcessInput().send(message)

        val payload = CancelProcessMessage(
                paymentTaskId = paymentTaskId,
                currentPaymentStatus = PaymentTransactionStatus.APPROVE_IN_PROGRESS.name
        )

        verify(processCancellationService).cancelPrepaymentProcess(payload)
    }

    @Test
    fun `receiveMessage should not do anything if count is greater than maxRetry`() {
        val message = MessageBuilder
                .withPayload(jsonMessage)
                .setHeader(X_DEATH_HEADER, mapOf("count" to 11.toLong()))
                .build()
        messagingSource.cancelPrepaymentProcessInput().send(message)

        verifyZeroInteractions(processCancellationService)
    }

    //@TODO remove this method in refund refactoring release 2 https://jira.lmru.tech/browse/PAY-569
    @Test
    fun `receiveMessage successful (oldImplementation)`() {
        val message = MessageBuilder.withPayload(jsonMessage).build()
        messagingSource.cancelSberlinkWithTpnetDepositWorkflowInput().send(message)

        val payload = CancelProcessMessage(
                paymentTaskId = paymentTaskId,
                currentPaymentStatus = PaymentTransactionStatus.APPROVE_IN_PROGRESS.name
        )

        verify(processCancellationService).cancelPrepaymentProcess(payload)
    }

    //@TODO remove this method in refund refactoring release 2 https://jira.lmru.tech/browse/PAY-569
    @Test
    fun `receiveMessage should not do anything if count is greater than maxRetry (oldImplementation)`() {
        val message = MessageBuilder
                .withPayload(jsonMessage)
                .setHeader(X_DEATH_HEADER, mapOf("count" to 11.toLong()))
                .build()
        messagingSource.cancelSberlinkWithTpnetDepositWorkflowInput().send(message)

        verifyZeroInteractions(processCancellationService)
    }

    @Language("JSON")
    val jsonMessage = """
            {
                "paymentTaskId": $paymentTaskId,
                "currentPaymentStatus": "APPROVE_IN_PROGRESS"
            }
        """.trimIndent()
}
