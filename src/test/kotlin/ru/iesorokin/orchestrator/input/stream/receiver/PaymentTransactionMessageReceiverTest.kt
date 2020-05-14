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
import ru.iesorokin.payment.orchestrator.core.service.prepayment.PaymentTransactionService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.PaymentTransactionMessage

class PaymentTransactionMessageReceiverTest : BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var messagingSource: MessagingSource
    @MockBean
    private lateinit var service: PaymentTransactionService
    private val paymentTransaction = "paymentTransaction"

    @Test
    fun `receivePaymentTransactionMessage successful`() {
        @Language("json")
        val json = """
            {
                "paymentTransaction": "paymentTransaction",
                "status": "HOLD"
            }
        """.trimIndent()

        messagingSource
                .paymentTransactionStatusModifiedInput()
                .send(MessageBuilder.withPayload(json).build())

        verify(service).processMessage(
                PaymentTransactionMessage(
                        paymentTransaction = paymentTransaction,
                        status = PaymentTransactionStatus.HOLD
                )
        )
    }

    @Test
    fun `should not do anything if count more than maxRetry`() {
        @Language("json")
        val json = """
            {
                "paymentTransaction": "paymentTransaction",
                "status": "HOLD"
            }
        """.trimIndent()

        val message = MessageBuilder.withPayload(json)
                .setHeader(X_DEATH_HEADER, mapOf("count" to 300.toLong()))
                .build()

        messagingSource.paymentTransactionStatusModifiedInput().send(message)

        verifyZeroInteractions(service)
    }
}
