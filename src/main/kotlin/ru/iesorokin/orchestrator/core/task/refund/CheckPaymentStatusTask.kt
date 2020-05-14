package ru.iesorokin.orchestrator.core.task.refund

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.core.service.ValidationService
import ru.iesorokin.orchestrator.core.task.variable
import ru.iesorokin.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class CheckPaymentStatusTask(
        val paymentTaskService: PaymentTaskService,
        val validationService: ValidationService
) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        val task = paymentTaskService.getPaymentTask(paymentTaskId)
        val taskStatus = PaymentTransactionStatus.valueOf(task.taskStatus)

        validationService.checkPaymentStatusForRefund(paymentTaskId, taskStatus)
        log.info { "Success checking payment status for taskId: $paymentTaskId" }
    }
}
