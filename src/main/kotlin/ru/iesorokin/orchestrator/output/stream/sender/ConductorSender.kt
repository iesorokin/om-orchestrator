package ru.iesorokin.payment.orchestrator.output.stream.sender

import mu.KotlinLogging
import org.springframework.integration.support.MessageBuilder
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.config.CONDUCT_BUSINESS_PROCESS_OUTPUT
import ru.iesorokin.payment.orchestrator.config.MessagingSource
import ru.iesorokin.payment.orchestrator.output.stream.sender.constants.REFUND_ROUTING_KEY
import ru.iesorokin.payment.orchestrator.output.stream.sender.constants.ROUTE_TO
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.conductor.RefundMessage

private val log = KotlinLogging.logger {}

@Service
class ConductorSender(private val messagingSource: MessagingSource) {

    fun sendRefundMessage(message: RefundMessage) {
        val wasSent = messagingSource.conductBusinessProcessOutput().send(
                MessageBuilder
                        .withPayload(message)
                        .setHeader(ROUTE_TO, REFUND_ROUTING_KEY)
                        .build())
        if (!wasSent) {
            throw RuntimeException("Message was not sent to $CONDUCT_BUSINESS_PROCESS_OUTPUT. $message")
        }

        log.info { "Message was sent to start refund: $message" }
    }
}