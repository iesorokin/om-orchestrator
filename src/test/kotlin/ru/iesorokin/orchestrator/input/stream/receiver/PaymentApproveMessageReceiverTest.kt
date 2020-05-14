package ru.iesorokin.orchestrator.input.stream.receiver

import com.nhaarman.mockito_kotlin.verify
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.support.MessageBuilder
import ru.iesorokin.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.orchestrator.config.MessagingSource
import ru.iesorokin.orchestrator.core.service.prepayment.PaymentTaskCommand
import ru.iesorokin.orchestrator.core.service.prepayment.PaymentTaskCommandService

class PaymentApproveMessageReceiverTest: BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var messagingSource: MessagingSource
    @MockBean
    private lateinit var servicePayment: PaymentTaskCommandService

    private val paymentTaskId = "12345"

    @Test
    fun receiveApproveMessage() {
        @Language("json")
        val json = """
            {
              "paymentTaskId": "$paymentTaskId"
            }
        """.trimIndent()

        messagingSource.approvePaymentTaskInput().send(MessageBuilder.withPayload(json).build())

        verify(servicePayment).executeCommand(paymentTaskId, PaymentTaskCommand.APPROVE)
    }
}
