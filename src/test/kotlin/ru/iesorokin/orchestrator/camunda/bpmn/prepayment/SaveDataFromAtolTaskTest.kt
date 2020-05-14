package ru.iesorokin.orchestrator.camunda.bpmn.prepayment

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Before
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_DOCUMENT_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_ID
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_REGISTRATION_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_STATUS
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_STORAGE_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_UUID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement

class SaveDataFromAtolTaskTest : PrepaymentCamundaTest() {

    private val paymentTaskId = "12345678"
    private val fiscalDataAtolId = "atol-id"
    private val fiscalDataUuid = "atol-uuid"
    private val fiscalDataRcrRegistrationNumber = "ecrRegistrationNumber"
    private val fiscalDataFiscalDocumentNumber = 31926072L
    private val fiscalDataFiscalStorageNumber = "fiscalStorageNumber"
    private val fiscalDataStatus = "DONE"

    private val registerStatus = PaymentTaskRegisterStatus(
            atolId = fiscalDataAtolId,
            uuid = fiscalDataUuid,
            ecrRegistrationNumber = fiscalDataRcrRegistrationNumber,
            fiscalDocumentNumber = fiscalDataFiscalDocumentNumber,
            fiscalStorageNumber = fiscalDataFiscalStorageNumber,
            status = fiscalDataStatus
    )

    @Before
    fun setUp() {
        val contextVariables = mapOf(
                PAYMENT_TASK_ID to paymentTaskId,
                ATOL_REGISTER_ID to fiscalDataAtolId,
                ATOL_REGISTER_UUID to fiscalDataUuid,
                ATOL_REGISTER_REGISTRATION_NUMBER to fiscalDataRcrRegistrationNumber,
                ATOL_REGISTER_DOCUMENT_NUMBER to fiscalDataFiscalDocumentNumber,
                ATOL_REGISTER_STORAGE_NUMBER to fiscalDataFiscalStorageNumber,
                ATOL_REGISTER_STATUS to fiscalDataStatus
        )
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .setVariables(contextVariables)
                .startAfterActivity(BusinessProcessElement.RECEIVE_ATOL_SUCCESS_RECEIVE_TASK.code)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted
    }

    @Test
    fun `execute should patch registerData in payment task with 3 retires`() {
        whenever(paymentTaskService.updateRegisterStatus(paymentTaskId, registerStatus))
                .thenThrow(RuntimeException())

        assertJobRetry(retryCount = 3, retryMinutes = 5)
        BpmnAwareAssertions.assertThat(processInstance).hasNotPassed(BusinessProcessElement.SAVE_DATA_FROM_ATOL_TASK.code)
        verify(paymentTaskService, times(3)).updateRegisterStatus(paymentTaskId, registerStatus)

        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }

    @Test
    fun `execute should patch registerData in payment task with one retry`() {
        whenever(paymentTaskService.updateRegisterStatus(paymentTaskId, registerStatus))
                .thenThrow(RuntimeException())
                .thenAnswer {}

        val retries = executeJob(processInstance.processInstanceId)
        assertThat(retries).isEqualTo(2)
        assertLockExpirationTime(5)
        executeJob(processInstance.processInstanceId)

        BpmnAwareAssertions.assertThat(processInstance).hasPassed(BusinessProcessElement.SAVE_DATA_FROM_ATOL_TASK.code)
    }
}
