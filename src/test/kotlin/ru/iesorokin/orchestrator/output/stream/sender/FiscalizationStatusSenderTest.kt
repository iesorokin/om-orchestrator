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
import ru.iesorokin.payment.orchestrator.core.enums.FiscalizationStatus
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.FiscalizationStatusMessage
import ru.iesorokin.payment.readPayloadTo

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class FiscalizationStatusSenderTest {

    @Autowired
    private lateinit var messageCollector: MessageCollector
    @Autowired
    private lateinit var messagingSource: MessagingSource
    @Autowired
    private lateinit var fiscalizationStatusSender: FiscalizationStatusSender

    @Test
    fun `sendFiscalizationStatus should send message`() {
        val paymentTaskId = "paymentTaskId"
        val extOrderId = "extOrderId"
        val status = FiscalizationStatus.FISCALIZATION_STARTED

        fiscalizationStatusSender.sendFiscalizationStatus(paymentTaskId, extOrderId, status)

        val actualMessage = getSentFiscalizationMessage()
        val expectedPayload = FiscalizationStatusMessage(paymentTaskId, extOrderId, status.name)
        assertThat(actualMessage.readPayloadTo<FiscalizationStatusMessage>()).isEqualTo(expectedPayload)
    }


    @Test(expected = RuntimeException::class)
    fun `sendFiscalizationStatus should throw Exception if message was not sent`() {
        val messagingSourceMock = Mockito.mock(MessagingSource::class.java, Mockito.RETURNS_DEEP_STUBS)
        val paymentTaskId = "paymentTaskId"
        val extOrderId = "extOrderId"
        whenever(messagingSourceMock.fiscalizationStatusOutput().send(any())).thenReturn(false)
        fiscalizationStatusSender = FiscalizationStatusSender(messagingSourceMock)

        fiscalizationStatusSender.sendFiscalizationStatus(paymentTaskId, extOrderId, FiscalizationStatus.FISCALIZATION_STARTED)
    }

    private fun getSentFiscalizationMessage() = messageCollector.forChannel(messagingSource.fiscalizationStatusOutput()).poll()
}
