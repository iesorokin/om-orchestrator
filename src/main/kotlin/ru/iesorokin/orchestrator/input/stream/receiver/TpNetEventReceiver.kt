package ru.iesorokin.payment.orchestrator.input.stream.receiver

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.config.TP_NET_GIVE_AWAY_FAILED_INPUT
import ru.iesorokin.payment.orchestrator.config.TP_NET_GIVE_AWAY_SUCCESSS_INPUT
import ru.iesorokin.payment.orchestrator.config.TP_NET_REFUND_COMMAND_FAILED_INPUT
import ru.iesorokin.payment.orchestrator.config.TP_NET_REFUND_COMMAND_SUCCESS_INPUT
import ru.iesorokin.payment.orchestrator.config.TP_NET_TRANSACTION_COMMAND_FAILED_INPUT
import ru.iesorokin.payment.orchestrator.config.TP_NET_TRANSACTION_COMMAND_SUCCESSS_INPUT
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.DepositEventType
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.RefundEventType
import ru.iesorokin.payment.orchestrator.core.service.TpNetService
import ru.iesorokin.payment.orchestrator.core.service.giveaway.GiveAwayService
import ru.iesorokin.payment.orchestrator.core.service.refund.RefundTpNetService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.TpNetDepositEventMessage
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.TpNetGiveAwayEventMessage
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.TpNetRefundEventMessage

private val log = KotlinLogging.logger {}

@Service
class TpNetEventReceiver(@Value("\${orchestrator.receiver.tpNetDepositEvent.maxRetry:5}")
                         private val depositEventMaxRetry: Long,
                         @Value("\${orchestrator.receiver.tpNetRefundEvent.maxRetry:5}")
                         private val refundEventMaxRetry: Long,
                         @Value("\${orchestrator.receiver.tpNetGiveAwayEvent.maxRetry:5}")
                         private val giveAwayEventMaxRetry: Long,
                         private val tpNetService: TpNetService,
                         private val refundTpNetService: RefundTpNetService,
                         private val giveAwayService: GiveAwayService) {

    @StreamListener(TP_NET_TRANSACTION_COMMAND_FAILED_INPUT)
    fun handleTpnetDepositFailEventMessage(@Payload message: TpNetDepositEventMessage,
                                           @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(TP_NET_TRANSACTION_COMMAND_FAILED_INPUT, message)
        if (death.isDeadLetterCountOverflown(depositEventMaxRetry)) {
            log.deadLetterCountOverflownError(depositEventMaxRetry, TP_NET_TRANSACTION_COMMAND_FAILED_INPUT, message)
            return
        }
        tpNetService.handleDepositEvent(message.paymentTaskId, DepositEventType.FAIL)
    }

    @StreamListener(TP_NET_TRANSACTION_COMMAND_SUCCESSS_INPUT)
    fun handleTpnetDepositSuccessEventMessage(@Payload message: TpNetDepositEventMessage,
                                              @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(TP_NET_TRANSACTION_COMMAND_SUCCESSS_INPUT, message)
        if (death.isDeadLetterCountOverflown(depositEventMaxRetry)) {
            log.deadLetterCountOverflownError(depositEventMaxRetry, TP_NET_TRANSACTION_COMMAND_SUCCESSS_INPUT, message)
            return
        }
        tpNetService.handleDepositEvent(message.paymentTaskId, DepositEventType.SUCCESS)
    }

    @StreamListener(TP_NET_REFUND_COMMAND_FAILED_INPUT)
    fun handleTpnetRefundFailEventMessage(@Payload message: TpNetRefundEventMessage,
                                          @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(TP_NET_REFUND_COMMAND_FAILED_INPUT, message)
        if (death.isDeadLetterCountOverflown(refundEventMaxRetry)) {
            log.deadLetterCountOverflownError(refundEventMaxRetry, TP_NET_REFUND_COMMAND_FAILED_INPUT, message)
            return
        }
        refundTpNetService.handleRefundEvent(message.paymentTaskId, RefundEventType.FAIL)
    }

    @StreamListener(TP_NET_REFUND_COMMAND_SUCCESS_INPUT)
    fun handleTpnetRefundSuccessEventMessage(@Payload message: TpNetRefundEventMessage,
                                             @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(TP_NET_REFUND_COMMAND_SUCCESS_INPUT, message)
        if (death.isDeadLetterCountOverflown(refundEventMaxRetry)) {
            log.deadLetterCountOverflownError(refundEventMaxRetry, TP_NET_REFUND_COMMAND_SUCCESS_INPUT, message)
            return
        }
        refundTpNetService.handleRefundEvent(message.paymentTaskId, RefundEventType.SUCCESS)
    }

    @StreamListener(TP_NET_GIVE_AWAY_SUCCESSS_INPUT)
    fun handleTpnetGiveAwaySuccessEventMessage(@Payload message: TpNetGiveAwayEventMessage,
                                               @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(TP_NET_GIVE_AWAY_SUCCESSS_INPUT, message)
        if (death.isDeadLetterCountOverflown(giveAwayEventMaxRetry)) {
            log.deadLetterCountOverflownError(giveAwayEventMaxRetry, TP_NET_GIVE_AWAY_SUCCESSS_INPUT, message)
            return
        }
        giveAwayService.processSuccessGiveAway(message.paymentTaskId)
    }

    @StreamListener(TP_NET_GIVE_AWAY_FAILED_INPUT)
    fun handleTpnetGiveAwayFailEventMessage(@Payload message: TpNetGiveAwayEventMessage,
                                          @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(TP_NET_GIVE_AWAY_FAILED_INPUT, message)
        if (death.isDeadLetterCountOverflown(giveAwayEventMaxRetry)) {
            log.deadLetterCountOverflownError(giveAwayEventMaxRetry, TP_NET_GIVE_AWAY_FAILED_INPUT, message)
            return
        }
        giveAwayService.processFailedGiveAway(message.paymentTaskId)
    }
}

