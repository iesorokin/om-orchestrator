package ru.iesorokin.orchestrator.camunda.bpmn.pod

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.iesorokin.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.getFileAsObject
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.pod.base.PodCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.orchestrator.core.domain.PaymentTaskRegisterStatusLine
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.POD_PAYMENT
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodProcessElement.RECEIVE_ATOL_SUCCESS
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodProcessElement.UPDATE_TASK_BY_REGISTERED_TRANSACTION_TASK

class UpdateTaskByRegisteredTransactionTaskTest : PodCamundaTest() {

    private val correlationKey = "correlationKey"
    private val solutionId = "solutionId"
    private val paymentTaskId = "paymentTaskId"

    private fun startProcess() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(POD_PAYMENT.processName)
                .startBeforeActivity(UPDATE_TASK_BY_REGISTERED_TRANSACTION_TASK.code)
                .businessKey(correlationKey)
                .setVariable(EXT_ORDER_ID, solutionId)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted
    }

    @Test
    fun `when update task by registered transaction failed more then 3 times incident occurs`() {
        // Given
        val paymentTaskId = "paymentTaskId"
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenThrow(RuntimeException())

        // When
        startProcess()
        val retryCount = 3
        assertJobRetry(retryCount = retryCount, retryMinutes = 5)

        // Then
        BpmnAwareAssertions.assertThat(processInstance).hasNotPassed(UPDATE_TASK_BY_REGISTERED_TRANSACTION_TASK.code)
        verify(paymentTaskService, times(retryCount)).getPaymentTask(paymentTaskId)

        val incident = rule.runtimeService.createIncidentQuery()
                .processInstanceId(processInstance.processInstanceId).singleResult()
        assertNotNull(incident)

        rule.endProcess(processInstance)
    }

    @Test
    fun `should continue process if all is ok`() {
        // Given
        val task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-success.json")
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(task)

        // When
        startProcess()
        executeJob(processInstance.processInstanceId)

        // Then
        val registerStatus = PaymentTaskRegisterStatus(
                lines = task.lines.map {
                    PaymentTaskRegisterStatusLine(it.extLineId!!, it.quantity, it.unitAmountIncludingVat)
                }
        )
        verify(paymentTaskService).updateRegisterStatus(paymentTaskId, registerStatus)
        assertThat(processInstance).isWaitingAt(RECEIVE_ATOL_SUCCESS.code)
        rule.endProcess(processInstance)
    }
}