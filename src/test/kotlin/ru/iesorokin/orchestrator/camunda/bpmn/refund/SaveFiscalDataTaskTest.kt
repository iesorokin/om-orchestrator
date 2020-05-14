package ru.iesorokin.orchestrator.camunda.bpmn.refund

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import ru.iesorokin.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.getFileAsObject
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.refund.base.RefundCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_DOCUMENT_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_ID
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_REGISTRATION_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_STORAGE_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_UUID
import ru.iesorokin.orchestrator.core.constants.process.CURRENT_PAYMENT_STATUS
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.RefundProcessElement.RECEIVE_ATOL_REFUND_SUCCESS_RECEIVE_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.RefundProcessElement.SAVE_FISCAL_DATA_TASK

class SaveFiscalDataTaskTest : RefundCamundaTest() {
    private val businessKey = "businessKey"
    private val paymentTaskId = "paymentTaskId1"
    private val fiscalDataAtolId = "atol-id"
    private val fiscalDataUuid = "atol-uuid"
    private val fiscalDataRcrRegistrationNumber = "ecrRegistrationNumber"
    private val fiscalDataFiscalDocumentNumber = 31926072L
    private val fiscalDataFiscalStorageNumber = "fiscalStorageNumber"
    private val currentPaymentStatus = "APPROVE_IN_PROGRESS"
    private lateinit var task: PaymentTask

    @Test
    fun `execute should try execute task with 3 retries`() {
        startProcess(businessKey)
        whenever(paymentTaskService.getPaymentTask(paymentTaskId))
                .thenThrow(RuntimeException())

        assertJobRetry(retryCount = 3, retryMinutes = 5)
        BpmnAwareAssertions.assertThat(processInstance).hasNotPassed(SAVE_FISCAL_DATA_TASK.code)
        verify(paymentTaskService, times(3)).getPaymentTask(paymentTaskId)

        rule.endProcess(processInstance)
    }

    @Test
    fun `execute should patch registerData in payment task with one retry when workflowId is businessKey`() {

        startProcess(businessKey)
        task.refundStatusList!!.first().refundWorkflowId = businessKey
        val testTask = task.copy()
        val refunds = testTask.refundStatusList!!
        refunds.find { it.refundWorkflowId == businessKey }!!
                .apply {
                    this.atolId = fiscalDataAtolId
                    this.uuid = fiscalDataUuid
                    this.ecrRegistrationNumber = fiscalDataRcrRegistrationNumber
                    this.fiscalDocumentNumber = fiscalDataFiscalDocumentNumber
                    this.fiscalStorageNumber = fiscalDataFiscalStorageNumber
                }
        whenever(paymentTaskService.getPaymentTask(paymentTaskId))
                .thenReturn(task)
        whenever(paymentTaskService.updateRefundStatusList(paymentTaskId, refunds))
                .thenThrow(RuntimeException())
                .thenAnswer {}

        val retries = executeJob(processInstance.processInstanceId)
        Assertions.assertThat(retries).isEqualTo(2)
        assertLockExpirationTime(5)
        executeJob(processInstance.processInstanceId)

        verify(paymentTaskService, times(2)).updateRefundStatusList(paymentTaskId, refunds)
    }

    fun startProcess(businessKey: String?) {
        val contextVariables = mapOf(
                CURRENT_PAYMENT_STATUS to currentPaymentStatus,
                PAYMENT_TASK_ID to paymentTaskId,
                ATOL_REFUND_ID to fiscalDataAtolId,
                ATOL_REFUND_UUID to fiscalDataUuid,
                ATOL_REFUND_REGISTRATION_NUMBER to fiscalDataRcrRegistrationNumber,
                ATOL_REFUND_DOCUMENT_NUMBER to fiscalDataFiscalDocumentNumber,
                ATOL_REFUND_STORAGE_NUMBER to fiscalDataFiscalStorageNumber
        )
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .businessKey(businessKey)
                .setVariables(contextVariables)
                .startAfterActivity(RECEIVE_ATOL_REFUND_SUCCESS_RECEIVE_TASK.code)
                .execute()
        task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-success.json")
        BpmnAwareAssertions.assertThat(processInstance).isStarted
    }
}
