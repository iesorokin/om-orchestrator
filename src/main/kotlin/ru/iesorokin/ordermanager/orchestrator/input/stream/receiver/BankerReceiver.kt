package ru.iesorokin.ordermanager.orchestrator.input.stream.receiver

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.ordermanager.orchestrator.config.Banker_PAID_STATUS_INPUT
import ru.iesorokin.ordermanager.orchestrator.core.service.BankerService
import ru.iesorokin.ordermanager.orchestrator.core.service.StreamLoggerService
import ru.iesorokin.ordermanager.orchestrator.input.stream.receiver.dto.CorrelationMessage

@Service
class BankerReceiver(
        private val BankerService: BankerService,
        @Value("\${orchestrator.consumer.maxRetry}")
        private val maxRetry: Long,
        private val streamLogger: StreamLoggerService
) {
    @StreamListener(Banker_PAID_STATUS_INPUT)
    fun receiveBankerStatus(@Payload message: CorrelationMessage,
                                 @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        streamLogger.inputMessage(Banker_PAID_STATUS_INPUT, message)
        if (death.isDeadLetterCountOverflown(maxRetry)) {
            streamLogger.deadLetterCountOverflownError(maxRetry, Banker_PAID_STATUS_INPUT, message)
            return
        }

        BankerService.processBankerPaidStatusMessage(message)
    }
}