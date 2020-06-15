package ru.iesorokin.ordermanager.orchestrator.core.task.common

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.ordermanager.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.ordermanager.orchestrator.core.constants.process.PAYMENT_TASK_STATUS
import ru.iesorokin.ordermanager.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.ordermanager.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.ordermanager.orchestrator.core.task.variableLocal
import ru.iesorokin.ordermanager.orchestrator.sleuth.ExtractProcessDataFromDelegate

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
