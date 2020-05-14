package ru.iesorokin.orchestrator.core.service.prepayment

import mu.KotlinLogging
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent.CANCEL_PREPAYMENT_PROCESS
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.orchestrator.core.service.CamundaService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.CancelProcessMessage
import ru.iesorokin.orchestrator.sleuth.propagateOrchestrationData
import ru.iesorokin.utility.sleuthbase.MdcService

private val log = KotlinLogging.logger { }

@Service
class ProcessCancellationService(
        private val camundaService: CamundaService,
        private val mdcService: MdcService
) {
    fun cancelPrepaymentProcess(message: CancelProcessMessage) {
        if (shouldNotCancelProcess(message)) {
            log.info { "Process cancellation will be not executed by message: $message" }
            return
        }

        val processInstance = getProcessInstance(message.paymentTaskId)

        mdcService.propagateOrchestrationData(processInstance.processInstanceId, camundaService)

        correlateMessage(processInstance)

        log.info {
            "Cancellation message for PrePayment process with processInstanceId=" +
                    "${processInstance.processInstanceId} has successfully processed"
        }
    }

    private fun correlateMessage(processInstance: ProcessInstance) {
        if (processInstance.businessKey != null) {
            camundaService.createMessageCorrelation(processInstance.businessKey, CANCEL_PREPAYMENT_PROCESS)
        } else {
            //@TODO remove else block after when SBERBANK_PREPAYMENT_WITH_TPNET will start by businessKey and all processInstances without businessKey will be completed
            val subscription = camundaService.getSubscriptionByProcessInstanceIdAndEventName(
                    processInstance.processInstanceId,
                    CANCEL_PREPAYMENT_PROCESS.message
            )

            camundaService.executeEvent(subscription.eventName, subscription.executionId)
        }

    }

    private fun getProcessInstance(paymentTaskId: String): ProcessInstance =
            try {
                camundaService.getProcessInstanceByBusinessKey(paymentTaskId)
                //@TODO remove catch block after when SBERBANK_PREPAYMENT_WITH_TPNET will start by businessKey and all processInstances without businessKey will be completed
            } catch (ex: Throwable) {
                camundaService.getPrePaymentTpNetProcessByVariable(
                        PAYMENT_TASK_ID,
                        paymentTaskId
                )
            }

    private fun shouldNotCancelProcess(message: CancelProcessMessage) =
            PaymentTransactionStatus.COMPLETED.name == message.currentPaymentStatus
}
