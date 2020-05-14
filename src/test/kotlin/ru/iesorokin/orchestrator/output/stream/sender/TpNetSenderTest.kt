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
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.TpNetDepositCommandMessage
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.TpNetGiveAwayCommandMessage
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.TpNetRefundCommandMessage
import ru.iesorokin.payment.readPayloadTo
import kotlin.test.fail

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class TpNetSenderTest {

    @Autowired
    private lateinit var messageCollector: MessageCollector
    @Autowired
    private lateinit var messagingSource: MessagingSource
    @Autowired
    private lateinit var tpNetSender: TpNetSender

    @Test
    fun `sendProcessTpNetDepositCommandMessage should send message`() {
        val paymentTaskId = "paymentTaskId"

        tpNetSender.sendTpNetDepositCommandMessage(paymentTaskId)

        val actualMessage = getTpNetTransactionCommandSentMessage()
        val expectedPayload = TpNetDepositCommandMessage(paymentTaskId)
        assertThat(actualMessage.readPayloadTo<TpNetDepositCommandMessage>()).isEqualTo(expectedPayload)
    }

    @Test
    fun `sendTpNetRefundCommandMessage should send message`() {
        val paymentTaskId = "paymentTaskId"
        val refundWorkflowId = "refundWorkflowId"

        tpNetSender.sendTpNetRefundCommandMessage(paymentTaskId, refundWorkflowId)

        val actualMessage = getTpNetRefundCommandSentMessage()
        val expectedPayload = TpNetRefundCommandMessage(paymentTaskId, refundWorkflowId)
        assertThat(actualMessage.readPayloadTo<TpNetRefundCommandMessage>()).isEqualTo(expectedPayload)
    }

    @Test
    fun `sendTpNetGiveAwayCommandMessage should send message`() {
        val paymentTaskId = "paymentTaskId"

        tpNetSender.sendGiveAwayCommand(paymentTaskId)

        val actual = getSentMessage()
        val expectedPayload = TpNetGiveAwayCommandMessage(paymentTaskId)
        assertThat(actual.readPayloadTo<TpNetGiveAwayCommandMessage>()).isEqualTo(expectedPayload)
    }

    @Test
    fun `sendTpNetGiveAwayCommandMessage should throw Exception if not send message`() {
        val messagingSourceMock = Mockito.mock(MessagingSource::class.java, Mockito.RETURNS_DEEP_STUBS)
        val paymentTaskId = "paymentTaskId"
        whenever(messagingSourceMock.processTpNetGiveAwayCommandOutput().send(any())).thenReturn(false)
        tpNetSender = TpNetSender(messagingSourceMock)

        try {
            tpNetSender.sendGiveAwayCommand(paymentTaskId)
            fail("Should throw RuntimeException")
        } catch (ex: RuntimeException) {
        }
    }

    private fun getTpNetTransactionCommandSentMessage() = messageCollector.forChannel(messagingSource.processTpnetTransactionCommandOutput()).poll()
    private fun getTpNetRefundCommandSentMessage() = messageCollector.forChannel(messagingSource.processTpnetRefundCommandOutput()).poll()
    private fun getSentMessage() = messageCollector.forChannel(messagingSource.processTpNetGiveAwayCommandOutput()).poll()
}
