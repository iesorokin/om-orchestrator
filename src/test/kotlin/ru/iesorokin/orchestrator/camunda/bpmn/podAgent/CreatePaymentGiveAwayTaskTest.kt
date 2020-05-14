package ru.iesorokin.payment.orchestrator.camunda.bpmn.podAgent

import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.camunda.bpmn.podAgent.base.PodAgentCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.EXECUTION_STORE
import ru.iesorokin.payment.orchestrator.core.constants.process.GIVE_AWAY_ID
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.enums.Application
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement.CREATE_GIVEAWAY_TASK

class CreatePaymentGiveAwayTaskTest : PodAgentCamundaTest() {

    private val testGiveAwayId = "34567890"
    private val testExecutionStore = 59

    @Test
    fun `createPaymentGiveAwayTask passed correctly`() {
        // Given
        val testPaymentTask = buildTestTask()
        whenever(paymentTaskService.getPaymentTask(testPaymentTaskId)).thenReturn(
                testPaymentTask
        )
        whenever(paymentTaskService.createGiveAway(testPaymentTask)).thenReturn(
                testGiveAwayId
        )

        // When
        startPodAgentProcess(CREATE_GIVEAWAY_TASK)
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).hasPassed(CREATE_GIVEAWAY_TASK.code)
        verify(paymentTaskService, times(1)).getPaymentTask(testPaymentTaskId)
        verify(paymentTaskService, times(1)).createGiveAway(testPaymentTask, Application.ORCHESTRATOR.name)
        assertThat(processInstance).variables()
                .containsEntry(GIVE_AWAY_ID, testGiveAwayId)
                .containsEntry(EXECUTION_STORE, testExecutionStore)
        rule.endProcess(processInstance)
    }

    @Test
    fun `createPaymentGiveAwayTask fails after 3 retries`() {
        // Given
        whenever(paymentTaskService.getPaymentTask(testPaymentTaskId))
                .thenThrow(RuntimeException())

        // When
        startPodAgentProcess(CREATE_GIVEAWAY_TASK)

        // Then
        assertJobRetry(retryCount = 3, retryMinutes = 5)
        assertThat(processInstance).hasNotPassed(CREATE_GIVEAWAY_TASK.code)
        verify(paymentTaskService, times(3)).getPaymentTask(testPaymentTaskId)
        rule.endProcess(processInstance)
    }

    private fun buildTestTask() = PaymentTask(
            taskId = testPaymentTaskId,
            taskStatus = "anyStatus",
            taskType = "POD_AGENT",
            lines = mutableListOf(),
            executionStore = testExecutionStore
    )

}