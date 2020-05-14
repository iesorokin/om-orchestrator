package ru.iesorokin.payment.orchestrator.core.service.prepayment

import mu.KotlinLogging
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.constants.process.FULL_APPROVE_KEY
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.PAYMENT_APPROVED
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.PAYMENT_COMPLETE
import ru.iesorokin.payment.orchestrator.core.service.CamundaService
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.payment.orchestrator.sleuth.propagateOrchestrationData
import ru.iesorokin.utility.sleuthbase.MdcService

private val log = KotlinLogging.logger { }

@Service
class PaymentTaskCommandService(
        private val paymentTaskService: PaymentTaskService,
        private val camundaService: CamundaService,
        private val mdcService: MdcService
) {
    fun executeCommand(paymentTaskId: String, command: PaymentTaskCommand) {
        when (command) {
            PaymentTaskCommand.APPROVE -> executeCommandApprove(paymentTaskId)
            PaymentTaskCommand.COMPLETE -> executeCommandComplete(paymentTaskId)
        }.let { /*empty let to force a when statement to assert all options are matched in a when statement*/ }

    }

    private fun executeCommandComplete(paymentTaskId: String) {
        try {
            camundaService.createMessageCorrelation(paymentTaskId, PAYMENT_COMPLETE)
        } catch (ex: Throwable) {
            //@TODO remove catch block after when SBERBANK_PREPAYMENT_WITH_TPNET will start by businessKey and all processInstances without businessKey will be completed
            val processInstance = camundaService.getPrePaymentTpNetProcessByVariable(
                    PAYMENT_TASK_ID,
                    paymentTaskId
            )

            val event = camundaService.getSubscriptionByProcessInstanceIdAndEventName(
                    processInstance.processInstanceId,
                    PAYMENT_COMPLETE.message)
            camundaService.executeEvent(event.eventName, event.executionId)
        }
    }

    private fun executeCommandApprove(paymentTaskId: String) {
        val processInstance = getProcessInstance(paymentTaskId)

        mdcService.propagateOrchestrationData(processInstance.processInstanceId, camundaService)

        val task = paymentTaskService.getPaymentTask(paymentTaskId)

        val isFullApproveInContextFalse = camundaService.getVariable(processInstance.processInstanceId, FULL_APPROVE_KEY).isExistAndFalse()

        val isFullApprove = !task.lines.any { it.quantity != it.confirmedQuantity } && !isFullApproveInContextFalse

        correlateMessage(processInstance, isFullApprove)
    }

    private fun getProcessInstance(paymentTaskId: String): ProcessInstance =
            try {
                camundaService.getProcessInstanceByBusinessKey(paymentTaskId)
            } catch (ex: Throwable) {
                //@TODO remove catch block when SBERBANK_PREPAYMENT_WITH_TPNET will start by businessKey and all processInstances without businessKey will be completed
                camundaService.getPrePaymentTpNetProcessByVariable(
                        PAYMENT_TASK_ID,
                        paymentTaskId
                )
            }

    private fun correlateMessage(processInstance: ProcessInstance, isFullApprove: Boolean) {
        val contextToUpdate = mapOf(FULL_APPROVE_KEY to isFullApprove)

        if (processInstance.businessKey != null) {
            camundaService.createMessageCorrelation(processInstance.businessKey, PAYMENT_APPROVED, contextToUpdate)
        } else {
            /*@TODO remove else block when SBERBANK_PREPAYMENT_WITH_TPNET will start by businessKey and all processInstances without businessKey will be completed */
            val subscription = camundaService.getSubscriptionByProcessInstanceIdAndEventName(
                    processInstance.processInstanceId,
                    PAYMENT_APPROVED.message
            )

            camundaService.executeEvent(
                    subscription.eventName,
                    subscription.executionId,
                    contextToUpdate
            )
        }
    }
}

private fun Any?.isExistAndFalse() = this != null && this is Boolean && this == false

enum class PaymentTaskCommand {
    COMPLETE,
    APPROVE
}
