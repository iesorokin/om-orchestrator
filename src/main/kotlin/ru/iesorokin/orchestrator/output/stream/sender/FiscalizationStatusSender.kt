package ru.iesorokin.payment.orchestrator.output.stream.sender

import mu.KotlinLogging
import org.springframework.integration.support.MessageBuilder
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.config.FISCALIZATION_STATUS_OUTPUT
import ru.iesorokin.payment.orchestrator.config.MessagingSource
import ru.iesorokin.payment.orchestrator.core.enums.FiscalizationStatus
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.FiscalizationStatusMessage

private val log = KotlinLogging.logger {}

@Service
class FiscalizationStatusSender(private val messagingSource: MessagingSource) {

    fun sendFiscalizationStatus(paymentTaskId: String, extOrderId: String, status: FiscalizationStatus) {
        val message = FiscalizationStatusMessage(paymentTaskId, extOrderId, status.name)
        log.info { "Send to $FISCALIZATION_STATUS_OUTPUT fiscalization status message: $message" }
        val wasSent = messagingSource.fiscalizationStatusOutput().send(MessageBuilder.withPayload(message).build())

        if (!wasSent) {
            throw RuntimeException("Message was not sent to $FISCALIZATION_STATUS_OUTPUT")
        }
    }
}
