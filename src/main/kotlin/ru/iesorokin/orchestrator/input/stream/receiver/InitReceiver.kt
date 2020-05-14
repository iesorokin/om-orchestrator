package ru.iesorokin.payment.orchestrator.input.stream.receiver

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.config.CREATE_PAYMENT_TASK_WORKFLOW_INPUT
import ru.iesorokin.payment.orchestrator.core.service.prepayment.InitService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.StartProcessMessage

private val log = KotlinLogging.logger {}

@Service
class InitReceiver(private val initService: InitService,
                   @Value("\${orchestrator.consumer.maxRetry}")
                   private val maxRetry: Long) {

    @StreamListener(CREATE_PAYMENT_TASK_WORKFLOW_INPUT)
    fun receiveStartPaymentProcessMessage(@Payload message: StartProcessMessage,
                                          @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(CREATE_PAYMENT_TASK_WORKFLOW_INPUT, message)
        if (death.isDeadLetterCountOverflown(maxRetry)) {
            log.deadLetterCountOverflownError(maxRetry, CREATE_PAYMENT_TASK_WORKFLOW_INPUT, message)
            return
        }
        initService.initPrepaymentProcess(message)
    }

}
