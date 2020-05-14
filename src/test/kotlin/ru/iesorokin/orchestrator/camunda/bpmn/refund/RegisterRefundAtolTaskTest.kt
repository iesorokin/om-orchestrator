package ru.iesorokin.orchestrator.camunda.bpmn.refund

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.iesorokin.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.SOLUTION_FILE_PATH
import ru.iesorokin.getFileAsObject
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.refund.base.RefundCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_ID
import ru.iesorokin.orchestrator.core.constants.process.CURRENT_PAYMENT_STATUS
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.domain.Solution
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.*
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.RefundProcessElement
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.RefundProcessElement.RECEIVE_ATOL_REFUND_SUCCESS_RECEIVE_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.RefundProcessElement.REGISTER_REFUND_ATOL_TRANSACTION_TASK

class RegisterRefundAtolTaskTest : RefundCamundaTest() {
    private val solutionId = "solutionId"
    private val paymentTaskId = "paymentTaskId"
    private val atolId = "expectedAtolId"
    private val currentPaymentStatus = "APPROVE_IN_PROGRESS"

    @After
    fun after() {
        rule.endProcess(processInstance)
    }

    @Test
    fun `when registerRefundAtolTransactionServiceTask executed and fails then it occurs 3 times with timeout 5 minutes`() {
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenThrow(RuntimeException())

        processInstance = rule.runtimeService.createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .startBeforeActivity(REGISTER_REFUND_ATOL_TRANSACTION_TASK.code)
                .setVariable(EXT_ORDER_ID, solutionId)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariable(CURRENT_PAYMENT_STATUS, currentPaymentStatus)
                .execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted

        val retryCount = 3
        assertJobRetry(retryCount = retryCount, retryMinutes = 5)

        BpmnAwareAssertions.assertThat(processInstance).hasNotPassed(REGISTER_REFUND_ATOL_TRANSACTION_TASK.code)
        verify(paymentTaskService, times(retryCount)).getPaymentTask(paymentTaskId)

        val incident = rule.runtimeService.createIncidentQuery().processInstanceId(processInstance.processInstanceId).singleResult()
        assertNotNull(incident)
    }

    @Test
    fun `should continue process if all is ok`() {
        val businessKey = "businessKey"
        val task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-success.json")
        val solution = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")
        whenever(solutionService.getSolutionOrder(solutionId)).thenReturn(solution)
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(task)
        whenever(atolService.registerAtolRefund(any(), any(), any(), any())).thenReturn(atolId)

        processInstance = rule.runtimeService.createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .startBeforeActivity(REGISTER_REFUND_ATOL_TRANSACTION_TASK.code)
                .businessKey(businessKey)
                .setVariable(EXT_ORDER_ID, solutionId)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariable(CURRENT_PAYMENT_STATUS, currentPaymentStatus)
                .execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted
        executeJob(processInstance.processInstanceId)
        assertThat(rule.runtimeService.getVariable(processInstance.processInstanceId, ATOL_REFUND_ID))
                .isEqualTo(atolId)
        ProcessEngineTests.assertThat(processInstance).isWaitingAt(RECEIVE_ATOL_REFUND_SUCCESS_RECEIVE_TASK.code)
    }
}
