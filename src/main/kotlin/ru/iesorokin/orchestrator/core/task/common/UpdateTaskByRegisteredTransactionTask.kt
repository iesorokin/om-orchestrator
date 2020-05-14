package ru.iesorokin.orchestrator.core.task.common

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.orchestrator.core.domain.PaymentTaskRegisterStatusLine
import ru.iesorokin.orchestrator.core.enums.TaskType
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.core.task.variable
import ru.iesorokin.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class UpdateTaskByRegisteredTransactionTask(
        private val paymentTaskService: PaymentTaskService
) : JavaDelegate {

    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) = try {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        log.info { "Start updating register status in task for paymentTaskId: $paymentTaskId" }
        val task = paymentTaskService.getPaymentTask(paymentTaskId)

        paymentTaskService.updateRegisterStatus(paymentTaskId, PaymentTaskRegisterStatus(
                lines = task.lines.map {
                    val quantity = if (task.taskType == TaskType.POD_POST_PAYMENT.name) it.confirmedQuantity else it.quantity
                    PaymentTaskRegisterStatusLine(it.extLineId!!, quantity, it.unitAmountIncludingVat)
                }
        ))
        log.info { "Register status was successfully updated for paymentTaskId: $paymentTaskId" }
    } catch (e: Exception) {
        log.error(e) { "Error occurred while update paymentTask register status for businessKey: ${execution.businessKey}" }
        throw e
    }

}
