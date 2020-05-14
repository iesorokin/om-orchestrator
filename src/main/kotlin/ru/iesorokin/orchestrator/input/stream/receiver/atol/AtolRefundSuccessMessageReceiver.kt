package ru.iesorokin.payment.orchestrator.input.stream.receiver.atol

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.config.ATOL_TRANSACTION_STATUS_MODIFIED_REFUND_INPUT
import ru.iesorokin.payment.orchestrator.core.service.refund.AtolRefundSuccessMessageService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.X_DEATH_HEADER
import ru.iesorokin.payment.orchestrator.input.stream.receiver.deadLetterCountOverflownError
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.AtolTransactionMessage
import ru.iesorokin.payment.orchestrator.input.stream.receiver.isDeadLetterCountOverflown

private val log = KotlinLogging.logger {}

@Service
class AtolRefundSuccessMessageReceiver(
        val atolRefundSuccessMessageService: AtolRefundSuccessMessageService,
        @Value("\${orchestrator.consumer.maxRetry}")
        private val maxRetry: Long
) {

    @StreamListener(ATOL_TRANSACTION_STATUS_MODIFIED_REFUND_INPUT)
    fun receiveApproveMessage(@Payload message: AtolTransactionMessage,
                              @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.info { "Received in queue: $ATOL_TRANSACTION_STATUS_MODIFIED_REFUND_INPUT message: $message" }
        if (death.isDeadLetterCountOverflown(maxRetry)) {
            log.deadLetterCountOverflownError(maxRetry, ATOL_TRANSACTION_STATUS_MODIFIED_REFUND_INPUT, message)
            return
        }
        atolRefundSuccessMessageService.processMessage(message)
    }
}
