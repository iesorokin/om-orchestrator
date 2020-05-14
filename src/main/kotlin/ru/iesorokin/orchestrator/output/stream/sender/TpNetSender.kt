package ru.iesorokin.orchestrator.output.stream.sender

import mu.KotlinLogging
import org.springframework.integration.support.MessageBuilder
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.config.MessagingSource
import ru.iesorokin.orchestrator.config.PROCESS_TPNET_REFUND_COMMAND_OUTPUT
import ru.iesorokin.orchestrator.config.PROCESS_TPNET_TRANSACTION_COMMAND_OUTPUT
import ru.iesorokin.orchestrator.config.PROCESS_TP_NET_GIVE_AWAY_COMMAND_OUTPUT
import ru.iesorokin.orchestrator.output.stream.sender.dto.TpNetDepositCommandMessage
import ru.iesorokin.orchestrator.output.stream.sender.dto.TpNetGiveAwayCommandMessage
import ru.iesorokin.orchestrator.output.stream.sender.dto.TpNetRefundCommandMessage

private val log = KotlinLogging.logger {}

@Service
class TpNetSender(private val messagingSource: MessagingSource) {

    fun sendTpNetDepositCommandMessage(paymentTaskId: String) {
        val message = TpNetDepositCommandMessage(paymentTaskId)
        log.info { "Send tpNet deposit command: $message" }

        val wasSent = messagingSource.processTpnetTransactionCommandOutput().send(
                MessageBuilder
                        .withPayload(message).build())
        if (!wasSent) {
            throw RuntimeException("Message not sent to $PROCESS_TPNET_TRANSACTION_COMMAND_OUTPUT")
        }
    }

    fun sendTpNetRefundCommandMessage(paymentTaskId: String, refundWorkflowId: String) {
        val message = TpNetRefundCommandMessage(paymentTaskId, refundWorkflowId)
        log.info { "Send tpNet refund command: $message" }

        val wasSent = messagingSource.processTpnetRefundCommandOutput().send(
                MessageBuilder
                        .withPayload(message).build())
        if (!wasSent) {
            throw RuntimeException("Message not sent to $PROCESS_TPNET_REFUND_COMMAND_OUTPUT")
        }
    }

    fun sendGiveAwayCommand(paymentTaskId: String) {
        val message = TpNetGiveAwayCommandMessage(paymentTaskId)
        log.info { "Send tpNet giveAway command: $message" }

        val wasSent = messagingSource.processTpNetGiveAwayCommandOutput().send(
                MessageBuilder.withPayload(message).build()
        )
        if (!wasSent) {
            throw RuntimeException("Message not sent to $PROCESS_TP_NET_GIVE_AWAY_COMMAND_OUTPUT")
        }
    }
}
