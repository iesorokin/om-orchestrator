package ru.iesorokin.payment.orchestrator.core.task.common

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REGISTER_DOCUMENT_NUMBER
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REGISTER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REGISTER_REGISTRATION_NUMBER
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REGISTER_STATUS
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REGISTER_STORAGE_NUMBER
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REGISTER_UUID
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.payment.orchestrator.core.task.longVariable
import ru.iesorokin.payment.orchestrator.core.task.variable
import ru.iesorokin.payment.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class SaveDataFromAtolTask(private val paymentTaskService: PaymentTaskService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        val registerStatus = PaymentTaskRegisterStatus(
                atolId = execution.variable(ATOL_REGISTER_ID),
                uuid = execution.variable(ATOL_REGISTER_UUID),
                ecrRegistrationNumber = execution.variable(ATOL_REGISTER_REGISTRATION_NUMBER),
                fiscalDocumentNumber = execution.longVariable(ATOL_REGISTER_DOCUMENT_NUMBER),
                fiscalStorageNumber = execution.variable(ATOL_REGISTER_STORAGE_NUMBER),
                status = execution.variable(ATOL_REGISTER_STATUS)
        )
        log.info { "update registerSale  ${execution.processInstanceId} in payment task $paymentTaskId" }
        paymentTaskService.updateRegisterStatus(paymentTaskId, registerStatus)

        execution.removeVariables(listOf(
                ATOL_REGISTER_ID,
                ATOL_REGISTER_UUID,
                ATOL_REGISTER_REGISTRATION_NUMBER,
                ATOL_REGISTER_DOCUMENT_NUMBER,
                ATOL_REGISTER_STORAGE_NUMBER,
                ATOL_REGISTER_STATUS
        ))
    }
}
