package ru.iesorokin.orchestrator.input.stream.receiver

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.config.CANCEL_PREPAYMENT_PROCESS_INPUT
import ru.iesorokin.orchestrator.config.CANCEL_SBERLINK_WITH_TP_NET_DEPOSIT_WORKFLOW_INPUT
import ru.iesorokin.orchestrator.core.service.prepayment.ProcessCancellationService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.CancelProcessMessage

private val log = KotlinLogging.logger { }

@Service
class ProcessCancellationReceiver(
        private val processCancellationService: ProcessCancellationService,
        @Value("\${orchestrator.consumer.maxRetry}")
        private val maxRetry: Long
) {

    @StreamListener(CANCEL_PREPAYMENT_PROCESS_INPUT)
    fun receiveMessage(@Payload message: CancelProcessMessage,
                       @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(CANCEL_PREPAYMENT_PROCESS_INPUT, message)
        if (death.isDeadLetterCountOverflown(maxRetry)) {
            log.deadLetterCountOverflownError(maxRetry, CANCEL_PREPAYMENT_PROCESS_INPUT, message)
            return
        }
        processCancellationService.cancelPrepaymentProcess(message)
    }

    //@TODO remove this method in refund refactoring release 2 https://jira.lmru.tech/browse/PAY-569
    @StreamListener(CANCEL_SBERLINK_WITH_TP_NET_DEPOSIT_WORKFLOW_INPUT)
    fun receiveMessageOldImplementation(@Payload message: CancelProcessMessage,
                                        @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(CANCEL_SBERLINK_WITH_TP_NET_DEPOSIT_WORKFLOW_INPUT, message)
        if (death.isDeadLetterCountOverflown(maxRetry)) {
            log.deadLetterCountOverflownError(maxRetry, CANCEL_SBERLINK_WITH_TP_NET_DEPOSIT_WORKFLOW_INPUT, message)
            return
        }
        processCancellationService.cancelPrepaymentProcess(message)
    }
}
