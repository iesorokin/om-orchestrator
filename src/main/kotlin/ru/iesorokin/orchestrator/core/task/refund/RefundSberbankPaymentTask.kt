package ru.iesorokin.orchestrator.core.task.refund

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.BpmnErrors
import ru.iesorokin.orchestrator.core.service.refund.RefundSberbankService
import ru.iesorokin.orchestrator.core.task.variable
import ru.iesorokin.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class RefundSberbankPaymentTask(private val refundSberbankService: RefundSberbankService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        val orderId = execution.variable(EXT_ORDER_ID)
        val workflowId = execution.businessKey

        try {
            log.info { "Do sberbank refund for paymentTaskId: $paymentTaskId orderId: $orderId workflowId: $workflowId"}
            refundSberbankService.doRefund(workflowId, paymentTaskId, orderId)
        } catch (e: Exception) {
            log.error(e) { "Error on sberbank refund paymentTaskId: $paymentTaskId orderId: $orderId workflowId: $workflowId" }
            throw BpmnErrors.REFUND_SBERBANK_ERROR.toBpmnError(e)
        }
    }
}
