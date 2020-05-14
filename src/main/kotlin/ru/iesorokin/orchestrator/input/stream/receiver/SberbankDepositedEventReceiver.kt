package ru.iesorokin.orchestrator.input.stream.receiver

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.config.SBERBANK_TRANSACTION_DEPOSITED_INPUT
import ru.iesorokin.orchestrator.core.enums.bpmn.DepositEventType
import ru.iesorokin.orchestrator.core.service.prepayment.SberbankDepositService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.SberbankDepositEventMessage

private val log = KotlinLogging.logger {}

internal const val ROUTE_KEY_FAIL = "sberbankTransactionDeposited.fail"
internal const val ROUTE_KEY_SUCESS = "sberbankTransactionDeposited.success"

@Service
class SberbankDepositedEventReceiver(@Value("\${orchestrator.receiver.sberbankDepositEvent.maxRetry:5}")
                                     private val maxRetry: Long,
                                     private val sberbankDepositService: SberbankDepositService) {

    @StreamListener(SBERBANK_TRANSACTION_DEPOSITED_INPUT)
    fun handleDepositEventMessage(@Payload message: SberbankDepositEventMessage,
                                  @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?,
                                  @Header(name = ROUTE_KEY_HEADER, required = true) routingKey: String) {
        log.logInputMessage(SBERBANK_TRANSACTION_DEPOSITED_INPUT, message)
        if (death.isDeadLetterCountOverflown(maxRetry)) {
            log.deadLetterCountOverflownError(maxRetry, SBERBANK_TRANSACTION_DEPOSITED_INPUT, message)
            return
        }

        val type = if (isFail(routingKey)) {
            log.warn { "Received deposit event message with error: $message" }
            DepositEventType.FAIL
        } else {
            DepositEventType.SUCCESS
        }

        sberbankDepositService.handleDepositEvent(message, type)
    }

    private fun isFail(routingKey: String): Boolean {
        return when {
            routingKey.equals(ROUTE_KEY_FAIL, true) -> true
            routingKey.equals(ROUTE_KEY_SUCESS, true) -> false
            else -> throw IllegalArgumentException("Invalid routingKey: $routingKey")
        }
    }
}

