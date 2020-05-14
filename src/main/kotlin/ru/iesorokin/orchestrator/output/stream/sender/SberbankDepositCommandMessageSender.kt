package ru.iesorokin.payment.orchestrator.output.stream.sender

import mu.KotlinLogging
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.config.MessagingSource
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.SberbankDepositCommandMessage
import java.math.BigDecimal

private val log = KotlinLogging.logger {}

@Service
class SberbankDepositCommandMessageSender(private val messagingSource: MessagingSource) {

    fun sendDepositComand(orderId: String, depositAmount: BigDecimal, storeId: Int, correlationKey: String? = null) {
        val message = SberbankDepositCommandMessage(orderId, depositAmount, storeId, correlationKey)
        log.info { "Send deposit command: $message" }

        messagingSource.createSberbankDepositTransactionOutput().send(
                MessageBuilder
                        .withPayload(message).build())
    }
}
