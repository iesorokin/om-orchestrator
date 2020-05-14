package ru.iesorokin.orchestrator.output.stream.sender

import mu.KotlinLogging
import org.springframework.integration.support.MessageBuilder
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.config.CREATE_ITSM_TICKET_OUTPUT
import ru.iesorokin.orchestrator.config.MessagingSource
import ru.iesorokin.orchestrator.core.domain.TpnetItsmTicket
import ru.iesorokin.orchestrator.output.stream.sender.constants.ITSM_TICKET_TPNET_ROUTING_KEY
import ru.iesorokin.orchestrator.output.stream.sender.constants.ROUTE_TO
import ru.iesorokin.orchestrator.output.stream.sender.dto.TpnetItsmTicketMessage

private val log = KotlinLogging.logger {}

@Service
class ItsmSender(private val messagingSource: MessagingSource) {

    fun sendTpnetItsmTicketMessage(tpnetItsmTicket: TpnetItsmTicket) {
        val message = TpnetItsmTicketMessage(
            paymentTaskId = tpnetItsmTicket.paymentTaskId,
            processInstanceId = tpnetItsmTicket.processInstanceId,
            tpnetOperationType = tpnetItsmTicket.tpnetOperationType
        )

        log.info { "Send ITSM ticket message: $message" }

        val wasSent = messagingSource.createItsmTicketOutput().send(
                MessageBuilder
                        .withPayload(message)
                        .setHeader(ROUTE_TO, ITSM_TICKET_TPNET_ROUTING_KEY)
                        .build())
        if (!wasSent) {
            throw RuntimeException("Message not sent to $CREATE_ITSM_TICKET_OUTPUT")
        }

        log.info { "Message: $message was sent to createItsmTicket" }
    }
}
