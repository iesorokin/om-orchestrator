package ru.iesorokin.payment.orchestrator.core.task.prepayment

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.payment.orchestrator.core.task.variable
import ru.iesorokin.payment.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class SaveWorkflowIdTask(private val paymentTaskService: PaymentTaskService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        log.info { "update workflow id ${execution.processInstanceId} in payment task $paymentTaskId" }
        paymentTaskService.updateWorkflowId(paymentTaskId, execution.processInstanceId)
    }
}
