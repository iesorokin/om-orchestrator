package ru.iesorokin.orchestrator.camunda.bpmn.prepayment

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Before
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.SAVE_WORKFLOW_ID_TASK

class SaveWorkflowIdTaskTest : PrepaymentCamundaTest() {

    private val paymentTaskId = "12345678"

    @Before
    fun setUp() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .execute()
    }

    @Test
    fun `execute should update workflowId in payment task with 5 retires`() {
        whenever(paymentTaskService.updateWorkflowId(paymentTaskId, processInstance.processInstanceId)).thenThrow(RuntimeException())

        assertJobRetry(retryCount = 5, retryMinutes = 5)
        BpmnAwareAssertions.assertThat(processInstance).hasNotPassed(SAVE_WORKFLOW_ID_TASK.code)
        verify(paymentTaskService, times(5)).updateWorkflowId(paymentTaskId, processInstance.processInstanceId)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }

    @Test
    fun `execute should update workflowId in payment task with retries`() {
        whenever(paymentTaskService.updateWorkflowId(paymentTaskId, processInstance.processInstanceId))
                .thenThrow(RuntimeException())
                .thenAnswer {}

        val retries = executeJob(processInstance.processInstanceId)
        assertThat(retries).isEqualTo(4)
        assertLockExpirationTime(5)
        executeJob(processInstance.processInstanceId)

        BpmnAwareAssertions.assertThat(processInstance).hasPassed(SAVE_WORKFLOW_ID_TASK.code)
        verify(paymentTaskService, times(2)).updateWorkflowId(paymentTaskId, processInstance.processInstanceId)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }


}
