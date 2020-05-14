package ru.iesorokin.orchestrator.input.stream.receiver

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.config.PAYMENT_TRANSACTION_STATUS_MODIFIED_INPUT
import ru.iesorokin.orchestrator.core.service.prepayment.PaymentTransactionService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.PaymentTransactionMessage

private val log = KotlinLogging.logger { }

@Service
class PaymentTransactionMessageReceiver(
        private val paymentTransactionService: PaymentTransactionService,
        @Value("\${orchestrator.consumer.maxRetryPerDay}")
        private val maxRetryPerDay: Long
) {
    @StreamListener(PAYMENT_TRANSACTION_STATUS_MODIFIED_INPUT)
    fun receivePaymentTransactionMessage(@Payload message: PaymentTransactionMessage,
                                         @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(PAYMENT_TRANSACTION_STATUS_MODIFIED_INPUT, message)
        if (death.isDeadLetterCountOverflown(maxRetryPerDay)) {
            log.deadLetterCountOverflownError(maxRetryPerDay, PAYMENT_TRANSACTION_STATUS_MODIFIED_INPUT, message)
            return
        }
        paymentTransactionService.processMessage(message)
    }
}
