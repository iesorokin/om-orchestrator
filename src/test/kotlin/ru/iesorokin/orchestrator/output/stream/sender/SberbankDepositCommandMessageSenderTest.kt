package ru.iesorokin.payment.orchestrator.output.stream.sender

import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.test.binder.MessageCollector
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import ru.iesorokin.payment.orchestrator.config.MessagingSource
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.SberbankDepositCommandMessage
import ru.iesorokin.payment.readPayloadTo

@RunWith(SpringRunner::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class SberbankDepositCommandMessageSenderTest {

    @Autowired
    private lateinit var messageCollector: MessageCollector
    @Autowired
    private lateinit var messagingSource: MessagingSource
    @Autowired
    private lateinit var sberbankDepositCommandSender: SberbankDepositCommandMessageSender

    @Test
    fun `sendDepositComand should send message`() {
        val orderId = "orderId"
        val depositAmount = "10.13".toBigDecimal()
        val storeId = 10
        val correlationKey = "correlationKey"

        sberbankDepositCommandSender.sendDepositComand(orderId, depositAmount, storeId, correlationKey)

        val actualMessage = getSentMessage()
        val expectedPayload = SberbankDepositCommandMessage(orderId, depositAmount, storeId, correlationKey)
        Assertions.assertThat(actualMessage.readPayloadTo<SberbankDepositCommandMessage>()).isEqualTo(expectedPayload)
    }

    private fun getSentMessage() = messageCollector.forChannel(messagingSource.createSberbankDepositTransactionOutput()).poll()
}
