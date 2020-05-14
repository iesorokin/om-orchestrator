package ru.iesorokin.payment.orchestrator.core.task.refund

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.service.refund.RefundTpNetService
import ru.iesorokin.payment.orchestrator.core.task.variable
import ru.iesorokin.payment.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class RefundTpNetTask(private val refundTpNetService: RefundTpNetService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        //TODO: remove `?: execution.processInstanceId` when all process instances on production without businessKey will be ended
        val workflowId = execution.businessKey ?: execution.processInstanceId

        log.info { "Start tpNet refund paymentTaskId: $paymentTaskId, workflowId: $workflowId" }

        refundTpNetService.doRefund(
                paymentTaskId = paymentTaskId,
                refundWorkflowId = workflowId
        )
    }
}
