package ru.iesorokin.payment.orchestrator.input.stream.receiver

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.camunda.bpm.engine.RuntimeService
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.support.MessageBuilder
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.config.MessagingSource
import ru.iesorokin.payment.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.service.prepayment.InitService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.StartProcessMessage

class InitReceiverTest : BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var messagingSource: MessagingSource
    @Autowired
    protected lateinit var runtimeService: RuntimeService
    @MockBean
    private lateinit var service: InitService

    @get:Rule
    val thrown = ExpectedException.none()!!

    val extOrderId = "1234"
    val paymentTaskId = "4321"

    @Test
    fun `should receive message and start process`() {

        @Language("JSON")
        val json = """
            {
                "workflowType": "SBERBANK_PREPAYMENT_WITH_TPNET",
                "extOrderId": $extOrderId,
                "paymentTaskId": $paymentTaskId
            }
        """.trimIndent()

        val message = MessageBuilder.withPayload(json)
                .build()
        messagingSource.createPaymentTaskWorkflowInput().send(message)

        verify(service).initPrepaymentProcess(
                StartProcessMessage(
                        SBERBANK_PREPAYMENT_WITH_TPNET,
                        "1234",
                        paymentTaskId
                )
        )
    }

    @Test
    fun `should not do anything if count is greater or equals maxRetry`() {
        @Language("JSON")
        val json = """
            {
                "workflowType": "SBERBANK_PREPAYMENT_WITH_TPNET",
                "extOrderId": $extOrderId,
                "paymentTaskId": $paymentTaskId
            }
        """.trimIndent()

        val message = MessageBuilder.withPayload(json)
                .setHeader(X_DEATH_HEADER, mapOf("count" to 10.toLong()))
                .build()

        messagingSource.createPaymentTaskWorkflowInput().send(message)

        MessageBuilder.withPayload(json)
                .setHeader(X_DEATH_HEADER, mapOf("count" to 11.toLong()))
                .build()

        messagingSource.createPaymentTaskWorkflowInput().send(message)

        verifyZeroInteractions(service)
    }

}
