package ru.iesorokin.orchestrator.core.task.refund

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_DOCUMENT_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_ID
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_REGISTRATION_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_STORAGE_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_UUID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.domain.PaymentTaskFiscalData
import ru.iesorokin.orchestrator.core.exception.EmptyFieldException
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.core.task.longVariable
import ru.iesorokin.orchestrator.core.task.variable
import ru.iesorokin.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class SaveFiscalDataTask(private val paymentTaskService: PaymentTaskService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        val workflowId = execution.businessKey
        val taskRefundStatusList = updateRefundStatusList(paymentTaskId, workflowId, execution)
        log.info { "Update registerSale for process $workflowId in payment task $paymentTaskId" }
        paymentTaskService.updateRefundStatusList(paymentTaskId, taskRefundStatusList)
        execution.removeVariable(ATOL_REFUND_UUID)
        execution.removeVariable(ATOL_REFUND_REGISTRATION_NUMBER)
        execution.removeVariable(ATOL_REFUND_DOCUMENT_NUMBER)
        execution.removeVariable(ATOL_REFUND_STORAGE_NUMBER)
    }

    private fun updateRefundStatusList(paymentTaskId: String, workflowId: String,
                                       execution: DelegateExecution): Collection<PaymentTaskFiscalData> {
        val taskRefundStatusList = paymentTaskService.getPaymentTask(paymentTaskId).refundStatusList
        val taskFiscalDataWithRefundWorkflowId = taskRefundStatusList
                ?.firstOrNull { workflowId == it.refundWorkflowId }
                ?: throw EmptyFieldException("fiscalData with workflowId $workflowId in task $paymentTaskId not found")
        taskFiscalDataWithRefundWorkflowId.apply {
            this.atolId = execution.variable(ATOL_REFUND_ID)
            this.uuid = execution.variable(ATOL_REFUND_UUID)
            this.ecrRegistrationNumber = execution.variable(ATOL_REFUND_REGISTRATION_NUMBER)
            this.fiscalDocumentNumber = execution.longVariable(ATOL_REFUND_DOCUMENT_NUMBER)
            this.fiscalStorageNumber = execution.variable(ATOL_REFUND_STORAGE_NUMBER)
        }
        return taskRefundStatusList
    }
}
