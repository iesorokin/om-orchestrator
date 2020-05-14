package ru.iesorokin.orchestrator.input.stream.receiver

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.config.PAYMENT_TASK_APPROVE_INPUT
import ru.iesorokin.orchestrator.core.service.prepayment.PaymentTaskCommand
import ru.iesorokin.orchestrator.core.service.prepayment.PaymentTaskCommandService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.PaymentApproveMessage

private val log = KotlinLogging.logger {}

@Service
class PaymentApproveMessageReceiver(
        private val paymentTaskCommandService: PaymentTaskCommandService,
        @Value("\${orchestrator.consumer.maxRetry}")
        private val maxRetry: Long
) {
    @StreamListener(PAYMENT_TASK_APPROVE_INPUT)
    fun receiveApproveMessage(@Payload message: PaymentApproveMessage,
                              @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(PAYMENT_TASK_APPROVE_INPUT, message)
        if (death.isDeadLetterCountOverflown(maxRetry)) {
            log.deadLetterCountOverflownError(maxRetry, PAYMENT_TASK_APPROVE_INPUT, message)
            return
        }
        paymentTaskCommandService.executeCommand(message.paymentTaskId, PaymentTaskCommand.APPROVE)
    }
}
