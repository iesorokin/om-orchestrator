package ru.iesorokin.payment.orchestrator.input.stream.receiver

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.support.MessageBuilder
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.config.MessagingSource
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.DepositEventType
import ru.iesorokin.payment.orchestrator.core.service.prepayment.SberbankDepositService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.SberbankDepositEventMessage

class SberbankDepositedEventReceiverTest: BaseSpringBootWithCamundaTest() {
    @Autowired
    private lateinit var messagingSource: MessagingSource
    @MockBean
    private lateinit var sberbankDepositService: SberbankDepositService

    private val orderId = "13123"
    private val message = "123"
    private val correlationKey = "correlationKey"

    @Test
    fun `handleDepositEventMessage should handle SUCCESS if routingKey sberbankTransactionDeposited_success`() {
        sendMessage(DepositEventType.SUCCESS)

        verify(sberbankDepositService).handleDepositEvent(SberbankDepositEventMessage(orderId, message, correlationKey), DepositEventType.SUCCESS)
    }

    @Test
    fun `handleDepositEventMessage should handle FAIL if routingKey sberbankTransactionDeposited_fail`() {
        sendMessage(DepositEventType.FAIL)

        verify(sberbankDepositService).handleDepositEvent(SberbankDepositEventMessage(orderId, message, correlationKey), DepositEventType.FAIL)
    }

    @Test
    fun `handleDepositEventMessage should not handle if exceed maxRetry`() {
        sendMessage(DepositEventType.FAIL, 100)

        verifyZeroInteractions(sberbankDepositService)
    }

    private fun sendMessage(status: DepositEventType, count: Long? = 0) = messagingSource.sberbankTransactionDepositedInput().send(
            MessageBuilder
                    .withPayload(SberbankDepositEventMessage(
                            orderId = orderId, message = message, correlationKey = correlationKey))
                    .setHeader(X_DEATH_HEADER, mapOf("count" to count))
                    .setHeader(ROUTE_KEY_HEADER, when(status) {
                        DepositEventType.FAIL -> ROUTE_KEY_FAIL
                        DepositEventType.SUCCESS -> ROUTE_KEY_SUCESS
                    })
                    .build()
    )
}
