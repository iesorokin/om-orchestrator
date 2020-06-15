package ru.iesorokin.ordermanager.orchestrator.output.stream.sender

import org.springframework.integration.support.MessageBuilder
import org.springframework.stereotype.Service
import ru.iesorokin.ordermanager.orchestrator.config.FISCALIZATION_STATUS_OUTPUT
import ru.iesorokin.ordermanager.orchestrator.config.MessagingSource
import ru.iesorokin.ordermanager.orchestrator.core.enums.FiscalizationStatus
import ru.iesorokin.ordermanager.orchestrator.core.service.StreamLoggerService
import ru.iesorokin.ordermanager.orchestrator.output.stream.sender.dto.FiscalizationStatusMessage

@Service
class FiscalizationStatusSender(private val messagingSource: MessagingSource,
                                private val streamLogger: StreamLoggerService) {

    fun sendFiscalizationStatus(paymentTaskId: String, extOrderId: String, status: FiscalizationStatus) {
        val message = FiscalizationStatusMessage(paymentTaskId, extOrderId, status.name)
        streamLogger.outputMessage(FISCALIZATION_STATUS_OUTPUT, message)

        val wasSent = messagingSource.fiscalizationStatusOutput().send(MessageBuilder.withPayload(message).build())
        if (!wasSent) {
            throw RuntimeException("Message was not sent to $FISCALIZATION_STATUS_OUTPUT. ${streamLogger.toJson(message)}")
        }
    }
}
