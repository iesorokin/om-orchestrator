package ru.iesorokin.payment.orchestrator.input.stream.receiver

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.config.BILLING_PAID_STATUS_INPUT
import ru.iesorokin.payment.orchestrator.core.service.BillingService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.CorrelationMessage

private val log = KotlinLogging.logger { }

@Service
class BillingReceiver(
        private val billingService: BillingService,
        @Value("\${orchestrator.consumer.maxRetry}")
        private val maxRetry: Long
) {
    @StreamListener(BILLING_PAID_STATUS_INPUT)
    fun receivePaidStatusMessage(@Payload message: CorrelationMessage,
                                 @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(BILLING_PAID_STATUS_INPUT, message)
        if (death.isDeadLetterCountOverflown(maxRetry)) {
            log.deadLetterCountOverflownError(maxRetry, BILLING_PAID_STATUS_INPUT, message)
            return
        }

        billingService.processBillingPaidStatusMessage(message)
    }
}