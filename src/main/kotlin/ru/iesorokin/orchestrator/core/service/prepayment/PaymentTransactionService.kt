package ru.iesorokin.orchestrator.core.service.prepayment

import mu.KotlinLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.orchestrator.core.service.CamundaService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.PaymentTransactionMessage
import ru.iesorokin.orchestrator.sleuth.propagateOrchestrationData
import ru.iesorokin.utility.sleuthbase.MdcService

private val log = KotlinLogging.logger { }

@Service
class PaymentTransactionService(
        private val camundaService: CamundaService,
        private val mdcService: MdcService
) {
    fun processMessage(message: PaymentTransactionMessage) {
        log.info { "Received message: $message" }

        val processInstance = camundaService.getPrePaymentTpNetProcessByVariable(
                EXT_ORDER_ID,
                message.paymentTransaction
        )

        mdcService.propagateOrchestrationData(processInstance.processInstanceId, camundaService)

        val eventName = when (message.status) {
            PaymentTransactionStatus.HOLD -> BusinessProcessEvent.PAYMENT_RECEIVED.message
            PaymentTransactionStatus.EXPIRED -> BusinessProcessEvent.PAYMENT_EXPIRED.message
            else -> throw ProcessEngineException("Current taskStatus=${message.status} but it must be: " +
                    "${PaymentTransactionStatus.HOLD} or ${PaymentTransactionStatus.EXPIRED}")
        }

        val subscription = camundaService.getSubscriptionByProcessInstanceIdAndEventName(
                processInstance.processInstanceId,
                eventName
        )

        camundaService.executeEvent(subscription.eventName, subscription.executionId)

        log.info {
            "Sberbank receive payment process with processInstanceId=${processInstance.processInstanceId} has successfully started"
        }
    }
}
