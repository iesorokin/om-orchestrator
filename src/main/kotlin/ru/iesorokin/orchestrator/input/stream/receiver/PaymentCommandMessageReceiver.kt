package ru.iesorokin.payment.orchestrator.input.stream.receiver

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.config.PAYMENT_ORCHESTRATOR_COMMAND_INPUT
import ru.iesorokin.payment.orchestrator.core.service.prepayment.PaymentTaskCommand
import ru.iesorokin.payment.orchestrator.core.service.prepayment.PaymentTaskCommandService

private val log = KotlinLogging.logger {}

@Service
class PaymentCommandMessageReceiver(
        private val paymentTaskCommandService: PaymentTaskCommandService,
        @Value("\${orchestrator.consumer.maxRetry}")
        private val maxRetry: Long
) {
    @StreamListener(PAYMENT_ORCHESTRATOR_COMMAND_INPUT)
    fun receiveCommandMessage(@Payload message: PaymentCommandMessage,
                              @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?,
                              @Header(name = ROUTE_KEY_HEADER, required = true) routingKey: String) {
        log.logInputMessage(PAYMENT_ORCHESTRATOR_COMMAND_INPUT, message)
        if (death.isDeadLetterCountOverflown(maxRetry)) {
            log.deadLetterCountOverflownError(maxRetry, PAYMENT_ORCHESTRATOR_COMMAND_INPUT, message)
            return
        }
        paymentTaskCommandService.executeCommand(message.paymentTaskId, routingKey.toCommand())
    }
}

private fun String.toCommand(): PaymentTaskCommand {
    return try {
        PaymentTaskCommand.valueOf(this.toUpperCase())
    } catch (e: Exception) {
        log.error { "Cannot determine payment command from routeKey $this" }
        throw e
    }
}

data class PaymentCommandMessage(
        val paymentTaskId: String
)
