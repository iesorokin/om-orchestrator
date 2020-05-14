package ru.iesorokin.orchestrator.output.stream.sender

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
import ru.iesorokin.orchestrator.config.MessagingSource
import ru.iesorokin.orchestrator.core.domain.TpnetItsmTicket
import ru.iesorokin.orchestrator.output.stream.sender.constants.ITSM_TICKET_TPNET_ROUTING_KEY
import ru.iesorokin.orchestrator.output.stream.sender.constants.ROUTE_TO
import ru.iesorokin.orchestrator.output.stream.sender.dto.TpnetItsmTicketMessage
import ru.iesorokin.readPayloadTo

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class ItsmSenderTest {

    @Autowired
    private lateinit var messageCollector: MessageCollector
    @Autowired
    private lateinit var messagingSource: MessagingSource
    @Autowired
    private lateinit var itsmSender: ItsmSender

    val paymentTaskId = "paymentTaskId"
    val processInstanceId = "processInstanceId"
    val tpnetOperationType = "DEPOSIT"

    @Test
    fun `sendItsmCreateIncidentCommandMessage should send message`() {
        itsmSender.sendTpnetItsmTicketMessage(testIncidentData())

        val actualMessage = getTpnetItsmTicketMessage()
        val expectedPayload = TpnetItsmTicketMessage(
            paymentTaskId = paymentTaskId,
            processInstanceId = processInstanceId,
            tpnetOperationType = tpnetOperationType
        )
        assertThat(actualMessage.readPayloadTo<TpnetItsmTicketMessage>()).isEqualTo(expectedPayload)
        assertThat(actualMessage.headers[ROUTE_TO]).isEqualTo(ITSM_TICKET_TPNET_ROUTING_KEY)
    }


    @Test(expected = RuntimeException::class)
    fun `sendItsmCreateIncidentCommandMessage should throw Exception if message was not sent`() {
        val messagingSourceMock = Mockito.mock(MessagingSource::class.java, Mockito.RETURNS_DEEP_STUBS)

        whenever(messagingSourceMock.createItsmTicketOutput().send(any())).thenReturn(false)
        itsmSender = ItsmSender(messagingSourceMock)

        itsmSender.sendTpnetItsmTicketMessage(testIncidentData())
    }

    private fun getTpnetItsmTicketMessage() = messageCollector.forChannel(messagingSource.createItsmTicketOutput()).poll()

    private fun testIncidentData() = TpnetItsmTicket(
            paymentTaskId = paymentTaskId,
            processInstanceId = processInstanceId,
            tpnetOperationType = tpnetOperationType
    )
}
