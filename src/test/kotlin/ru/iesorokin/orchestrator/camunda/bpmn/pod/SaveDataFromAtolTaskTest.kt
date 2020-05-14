package ru.iesorokin.orchestrator.camunda.bpmn.pod

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Before
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.pod.base.PodCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_DOCUMENT_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_ID
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_REGISTRATION_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_STATUS
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_STORAGE_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_UUID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodProcessElement.SAVE_DATA_FROM_ATOL
import kotlin.test.assertNull

class SaveDataFromAtolTaskTest : PodCamundaTest() {

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
    fun preparation() {
        val contextVariables = mapOf(
                PAYMENT_TASK_ID to testPaymentTaskId,
                ATOL_REGISTER_ID to fiscalDataAtolId,
                ATOL_REGISTER_UUID to fiscalDataUuid,
                ATOL_REGISTER_REGISTRATION_NUMBER to fiscalDataRcrRegistrationNumber,
                ATOL_REGISTER_DOCUMENT_NUMBER to fiscalDataFiscalDocumentNumber,
                ATOL_REGISTER_STORAGE_NUMBER to fiscalDataFiscalStorageNumber,
                ATOL_REGISTER_STATUS to fiscalDataStatus
        )

        startPodProcess(SAVE_DATA_FROM_ATOL, contextVariables)

        assertThat(processInstance).isStarted
    }

    @Test
    fun `saveDataFromAtolTask passed correctly`() {
        // When
        executeJob(processInstance.processInstanceId)

        // Then
        verify(paymentTaskService, times(1)).updateRegisterStatus(testPaymentTaskId, registerStatus)
        assertThat(processInstance).hasPassed(SAVE_DATA_FROM_ATOL.code)
        assertNull(rule.runtimeService.getVariable(processInstance.processInstanceId, ATOL_REGISTER_ID))
        assertNull(rule.runtimeService.getVariable(processInstance.processInstanceId, ATOL_REGISTER_UUID))
        assertNull(rule.runtimeService.getVariable(processInstance.processInstanceId, ATOL_REGISTER_REGISTRATION_NUMBER))
        assertNull(rule.runtimeService.getVariable(processInstance.processInstanceId, ATOL_REGISTER_DOCUMENT_NUMBER))
        assertNull(rule.runtimeService.getVariable(processInstance.processInstanceId, ATOL_REGISTER_STORAGE_NUMBER))
        assertNull(rule.runtimeService.getVariable(processInstance.processInstanceId, ATOL_REGISTER_STATUS))
        rule.endProcess(processInstance)
    }
}