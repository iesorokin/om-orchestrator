package ru.iesorokin.payment.orchestrator.output.stream.sender

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import ru.iesorokin.payment.orchestrator.config.MessagingSource
import ru.iesorokin.payment.orchestrator.output.stream.sender.constants.REFUND_ROUTING_KEY
import ru.iesorokin.payment.orchestrator.output.stream.sender.constants.ROUTE_TO
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.conductor.RefundMessage
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.conductor.RefundMessageLine
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.conductor.RefundType
import ru.iesorokin.payment.readPayloadTo
import java.math.BigDecimal

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class ConductorSenderTest {

    @Autowired
    private lateinit var messageCollector: MessageCollector
    @Autowired
    private lateinit var messagingSource: MessagingSource
    @Autowired
    private lateinit var conductorSender: ConductorSender

    val paymentTaskId = "paymentTaskId"
    val processInstanceId = "processInstanceId"

    @Test
    fun `sendRefundMessage should send message`() {
        val refundMessage = buildRefundMessage()
        conductorSender.sendRefundMessage(refundMessage)

        val actualMessage = actualQueueData()
        assertThat(actualMessage.readPayloadTo<RefundMessage>()).isEqualTo(refundMessage)
        assertThat(actualMessage.headers[ROUTE_TO]).isEqualTo(REFUND_ROUTING_KEY)
    }

    @Test(expected = RuntimeException::class)
    fun `sendRefundMessage should throw Exception if message was not sent`() {
        val messagingSourceMock = Mockito.mock(MessagingSource::class.java, Mockito.RETURNS_DEEP_STUBS)

        whenever(messagingSourceMock.conductBusinessProcessOutput().send(any())).thenReturn(false)
        conductorSender = ConductorSender(messagingSourceMock)

        conductorSender.sendRefundMessage(buildRefundMessage())
    }

    private fun buildRefundMessage(): RefundMessage =
            RefundMessage(
                    refundType = RefundType.PARTIAL,
                    extOrderId = "extOrderId",
                    paymentTaskId = "paymentTaskId",
                    currentPaymentStatus = "PAID",
                    taskType = "taskType",
                    lines = listOf(
                            RefundMessageLine(
                                    extLineId = "extLineId",
                                    quantity = BigDecimal.ONE,
                                    unitAmountIncludingVat = BigDecimal.ONE
                            )
                    )
            )

    private fun actualQueueData() = messageCollector.forChannel(messagingSource.conductBusinessProcessOutput()).poll()
}
