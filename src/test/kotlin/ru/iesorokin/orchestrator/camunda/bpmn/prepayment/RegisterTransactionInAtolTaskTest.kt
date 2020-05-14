package ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.iesorokin.payment.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.payment.SOLUTION_FILE_PATH
import ru.iesorokin.payment.getFileAsObject
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REGISTER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.Solution
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.REGISTER_TRANSACTION_IN_ATOL_TASK
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.UPDATE_TASK_BY_REGISTERED_TRANSACTION_TASK

class RegisterTransactionInAtolTaskTest : PrepaymentCamundaTest() {

    private val correlationKey = "correlationKey"
    private val solutionId = "solutionId"
    private val paymentTaskId = "paymentTaskId"

    private fun startProcess() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET.processName)
                .startBeforeActivity(REGISTER_TRANSACTION_IN_ATOL_TASK.code)
                .businessKey(correlationKey)
                .setVariable(EXT_ORDER_ID, solutionId)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted
    }

    @Test
    fun `when register transaction in atol failed more then 3 times incident occurs`() {
        // Given
        val paymentTaskId = "paymentTaskId"
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenThrow(RuntimeException())

        // When
        startProcess()
        val retryCount = 3
        assertJobRetry(retryCount = retryCount, retryMinutes = 5)

        // Then
        BpmnAwareAssertions.assertThat(processInstance).hasNotPassed(REGISTER_TRANSACTION_IN_ATOL_TASK.code)
        verify(paymentTaskService, times(retryCount)).getPaymentTask(paymentTaskId)

        val incident = rule.runtimeService.createIncidentQuery()
                .processInstanceId(processInstance.processInstanceId).singleResult()
        assertNotNull(incident)

        rule.endProcess(processInstance)
    }

    @Test
    fun `should continue process if all is ok`() {
        // Given
        val atolId = "expectedAtolId"
        val task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-success.json")
        val solution = getFileAsObject<Solution>("${SOLUTION_FILE_PATH}solution-success.json")
        whenever(solutionService.getSolutionOrder(solutionId)).thenReturn(solution)
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(task)
        whenever(atolService.registerAtolSale(task, solution, correlationKey)).thenReturn(atolId)

        // When
        startProcess()
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(rule.runtimeService.getVariable(processInstance.processInstanceId, ATOL_REGISTER_ID))
                .isEqualTo(atolId)
        ProcessEngineTests.assertThat(processInstance).isWaitingAt(UPDATE_TASK_BY_REGISTERED_TRANSACTION_TASK.code)
        rule.endProcess(processInstance)
    }

}
