package ru.iesorokin.orchestrator.camunda.bpmn.podAgent

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.podAgent.base.PodAgentCamundaTest
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus.DEPOSIT_AND_GIVEAWAY_DONE
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus.DEPOSIT_IN_PROGRESS
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus.GIVEAWAY_IN_PROGRESS
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus.PAID
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement.CHANGE_TASK_STATUS_TO_DEPOSIT_GIVEAWAY_DONE_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement.CHANGE_TASK_STATUS_TO_DEPOSIT_IN_PROGRESS_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement.CHANGE_TASK_STATUS_TO_GIVEAWAY_IN_PROGRESS_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement.CHANGE_TASK_STATUS_TO_PAID_TASK

class ChangePaymentTaskStatusTaskTest : PodAgentCamundaTest() {

    @Test
    fun `changeTaskStatusToDepositInProgressTask passed correctly`() {
        checkTaskPassedCorrectly(DEPOSIT_IN_PROGRESS, CHANGE_TASK_STATUS_TO_DEPOSIT_IN_PROGRESS_TASK)
    }

    @Test
    fun `changeTaskStatusToGiveAwayInProgressTask passed correctly`() {
        checkTaskPassedCorrectly(GIVEAWAY_IN_PROGRESS, CHANGE_TASK_STATUS_TO_GIVEAWAY_IN_PROGRESS_TASK)
    }

    @Test
    fun `changeTaskStatusToDepositGiveAwayDoneTask passed correctly`() {
        checkTaskPassedCorrectly(DEPOSIT_AND_GIVEAWAY_DONE, CHANGE_TASK_STATUS_TO_DEPOSIT_GIVEAWAY_DONE_TASK)
    }

    @Test
    fun `changeTaskStatusToPaidTask passed correctly`() {
        checkTaskPassedCorrectly(PAID, CHANGE_TASK_STATUS_TO_PAID_TASK)
    }

    @Test
    fun `changeTaskStatusToDepositInProgressTask fails after 3 retries`() {
        checkFailedScenarioPassedCorrectly(DEPOSIT_IN_PROGRESS, CHANGE_TASK_STATUS_TO_DEPOSIT_IN_PROGRESS_TASK)
    }

    @Test
    fun `changeTaskStatusToGiveAwayInProgressTask fails after 3 retries`() {
        checkFailedScenarioPassedCorrectly(GIVEAWAY_IN_PROGRESS, CHANGE_TASK_STATUS_TO_GIVEAWAY_IN_PROGRESS_TASK)
    }

    @Test
    fun `changeTaskStatusToDepositGiveAwayDoneTask fails after 3 retries`() {
        checkFailedScenarioPassedCorrectly(DEPOSIT_AND_GIVEAWAY_DONE, CHANGE_TASK_STATUS_TO_DEPOSIT_GIVEAWAY_DONE_TASK)
    }

    @Test
    fun `changeTaskStatusToPaidTask fails after 3 retries`() {
        checkFailedScenarioPassedCorrectly(PAID, CHANGE_TASK_STATUS_TO_PAID_TASK)
    }

    private fun checkTaskPassedCorrectly(status: PaymentTransactionStatus, task: PodAgentProcessElement) {
        // Given
        whenever(paymentTaskService.updateTaskStatus(testPaymentTaskId, status))
                .then { }

        // When
        startPodAgentProcess(task)
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).hasPassed(task.code)
        verify(paymentTaskService, times(1)).updateTaskStatus(testPaymentTaskId, status)
        rule.endProcess(processInstance)
    }

    private fun checkFailedScenarioPassedCorrectly(status: PaymentTransactionStatus, task: PodAgentProcessElement) {
        // Given
        whenever(paymentTaskService.updateTaskStatus(testPaymentTaskId, status))
                .thenThrow(RuntimeException())

        // When
        startPodAgentProcess(task)

        // Then
        assertJobRetry(retryCount = 3, retryMinutes = 5)
        assertThat(processInstance).hasNotPassed(task.code)
        verify(paymentTaskService, times(3)).updateTaskStatus(testPaymentTaskId, status)
        rule.endProcess(processInstance)
    }
}