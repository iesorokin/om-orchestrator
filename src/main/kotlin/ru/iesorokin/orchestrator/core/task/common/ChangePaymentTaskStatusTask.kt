package ru.iesorokin.orchestrator.core.task.common

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_STATUS
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.core.task.variableLocal
import ru.iesorokin.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class ChangePaymentTaskStatusTask(private val paymentTaskService: PaymentTaskService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variableLocal(PAYMENT_TASK_ID)
        val paymentTaskStatus = PaymentTransactionStatus.valueOf(execution.variableLocal(PAYMENT_TASK_STATUS))
        log.info { "Change payment task to $paymentTaskStatus status. WorkflowID ${execution.processInstanceId}, payment task $paymentTaskId" }

        paymentTaskService.updateTaskStatus(paymentTaskId, paymentTaskStatus)
    }
}
