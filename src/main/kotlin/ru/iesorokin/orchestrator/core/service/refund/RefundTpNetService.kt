package ru.iesorokin.payment.orchestrator.core.service.refund

import mu.KotlinLogging
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.RefundEventType
import ru.iesorokin.payment.orchestrator.core.service.CamundaService
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.payment.orchestrator.output.stream.sender.TpNetSender

private val log = KotlinLogging.logger {}

@Service
class RefundTpNetService(private val tpNetSender: TpNetSender,
                         private val paymentTaskService: PaymentTaskService,
                         private val camundaService: CamundaService) {

    fun doRefund(paymentTaskId: String, refundWorkflowId: String) =
            tpNetSender.sendTpNetRefundCommandMessage(paymentTaskId, refundWorkflowId)

    fun handleRefundEvent(paymentTaskId: String, refundEventType: RefundEventType) {
        log.info { "Handle refund event for paymentTaskId: $paymentTaskId and eventType $refundEventType" }

        val paymentTask = paymentTaskService.getPaymentTask(paymentTaskId)
        val processInstance = getProcessInstance(paymentTask)

        val subscription = camundaService.getSubscriptionByProcessInstanceIdAndEventName(
                processInstanceId = processInstance.processInstanceId,
                eventName = when (refundEventType) {
                    RefundEventType.SUCCESS -> BusinessProcessEvent.TPNET_REFUND_SUCCESS.message
                    RefundEventType.FAIL -> BusinessProcessEvent.TPNET_REFUND_FAIL.message
                })

        camundaService.executeEvent(subscription.eventName, subscription.executionId)
    }

    private fun getProcessInstance(paymentTask: PaymentTask): ProcessInstance {
        try {
            val sortedRefundWorkflowIds = getSortedRefundWorkflowIds(paymentTask)
                    ?: throw IllegalStateException("ProcessInstanceId not found in paymentTask: $paymentTask")

            val activeProcessInstances = camundaService.getActiveProcessInstances(Process.SBERBANK_REFUND_WITH_TPNET)
            return getFirstActiveRefundProcessInstance(activeProcessInstances, sortedRefundWorkflowIds)
                    ?: throw IllegalStateException(
                            "ProcessInstance ${Process.SBERBANK_REFUND_WITH_TPNET} not found for paymentTask: $paymentTask"
                    )
        } catch (e: Throwable) {
            log.error(e) {
                "Something went wrong trying to get processInstance with paymentTask: $paymentTask"
            }
            throw e
        }
    }


    private fun getSortedRefundWorkflowIds(paymentTask: PaymentTask): Collection<String?>? =
            paymentTask.refundStatusList
                    ?.sortedBy { it.created }
                    ?.map { it.refundWorkflowId }

    private fun getFirstActiveRefundProcessInstance(activeProcessInstances: List<ProcessInstance>,
                                                    refundWorkflowIds: Collection<String?>): ProcessInstance? {
        // TODO: added transform to map for activeProcessInstances when all processes have a business key
        refundWorkflowIds.forEach { refundWorkflowId ->
            activeProcessInstances.forEach { processInstance ->
                if (processInstance.businessKey == refundWorkflowId || processInstance.processInstanceId == refundWorkflowId){
                    return processInstance
                }
            }
        }
        return null
    }
}
