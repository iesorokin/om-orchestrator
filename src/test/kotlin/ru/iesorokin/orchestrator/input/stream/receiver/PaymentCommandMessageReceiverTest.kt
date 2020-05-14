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


class PaymentCommandMessageReceiverTest: BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var messagingSource: MessagingSource
    @MockBean
    private lateinit var servicePayment: PaymentTaskCommandService

    private val paymentTaskId = "12345"

    @Test
    fun `receiveCommandMessage should invoke APPROVE command if routeKey is approve`() {
        @Language("json")
        val json = """
            {
              "paymentTaskId": "$paymentTaskId"
            }
        """.trimIndent()

        messagingSource.paymentOrchestratorCommandInput().send(
                MessageBuilder
                        .withPayload(json)
                        .setHeader("routeTo", "approve")
                        .build())

        verify(servicePayment).executeCommand(paymentTaskId, PaymentTaskCommand.APPROVE)
    }

    @Test
    fun `receiveCommandMessage should invoke COMPLETE command if routeKey is complete`() {
        @Language("json")
        val json = """
            {
              "paymentTaskId": "$paymentTaskId"
            }
        """.trimIndent()

        messagingSource.paymentOrchestratorCommandInput().send(
                MessageBuilder
                        .withPayload(json)
                        .setHeader("routeTo", "complete")
                        .build())

        verify(servicePayment).executeCommand(paymentTaskId, PaymentTaskCommand.COMPLETE)
    }

}
