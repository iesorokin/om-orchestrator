package ru.iesorokin.payment.orchestrator.camunda.bpmn.pod

import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.After
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.base.PodCamundaTest
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.APPROVE_IN_PROGRESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.DEPOSIT_IN_PROGRESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.PAID
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement.CHANGE_TASK_STATUS_TO_APPROVE_IN_PROGRESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement.CHANGE_TASK_STATUS_TO_DEPOSIT_IN_PROGRESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement.CHANGE_TASK_STATUS_TO_PAID

class ChangePaymentTaskStatusTaskTest : PodCamundaTest() {

    @After
    fun after() {
        rule.endProcess(processInstance)
    }

    @Test
    fun `changePaymentTaskStatusToApproveInProgressTask passed correctly`() {
        // Given
        doNothing().`when`(paymentTaskService).updateTaskStatus(testPaymentTaskId, APPROVE_IN_PROGRESS)

        // When
        startPodProcess(CHANGE_TASK_STATUS_TO_APPROVE_IN_PROGRESS)
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).hasPassed(CHANGE_TASK_STATUS_TO_APPROVE_IN_PROGRESS.code)
        verify(paymentTaskService, times(1)).updateTaskStatus(testPaymentTaskId, APPROVE_IN_PROGRESS)
    }

    @Test
    fun `changePaymentTaskStatusToApproveInProgressTask fails after 3 retries`() {
        // Given
        whenever(paymentTaskService.updateTaskStatus(testPaymentTaskId, APPROVE_IN_PROGRESS))
                .thenThrow(RuntimeException())

        // When
        startPodProcess(CHANGE_TASK_STATUS_TO_APPROVE_IN_PROGRESS)

        // Then
        assertJobRetry(retryCount = 3, retryMinutes = 5)
        assertThat(processInstance).hasNotPassed(CHANGE_TASK_STATUS_TO_APPROVE_IN_PROGRESS.code)
        verify(paymentTaskService, times(3)).updateTaskStatus(testPaymentTaskId, APPROVE_IN_PROGRESS)
    }


    @Test
    fun `changePaymentTaskStatusToDepositInProgressTask passed correctly`() {
        // Given
        doNothing().`when`(paymentTaskService).updateTaskStatus(testPaymentTaskId, DEPOSIT_IN_PROGRESS)

        // When
        startPodProcess(CHANGE_TASK_STATUS_TO_DEPOSIT_IN_PROGRESS)
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).hasPassed(CHANGE_TASK_STATUS_TO_DEPOSIT_IN_PROGRESS.code)
        verify(paymentTaskService, times(1)).updateTaskStatus(testPaymentTaskId, DEPOSIT_IN_PROGRESS)
    }

    @Test
    fun `changePaymentTaskStatusToDepositInProgressTask fails after 3 retries`() {
        // Given
        whenever(paymentTaskService.updateTaskStatus(testPaymentTaskId, DEPOSIT_IN_PROGRESS))
                .thenThrow(RuntimeException())

        // When
        startPodProcess(CHANGE_TASK_STATUS_TO_DEPOSIT_IN_PROGRESS)

        // Then
        assertJobRetry(retryCount = 3, retryMinutes = 5)
        assertThat(processInstance).hasNotPassed(CHANGE_TASK_STATUS_TO_DEPOSIT_IN_PROGRESS.code)
        verify(paymentTaskService, times(3)).updateTaskStatus(testPaymentTaskId, DEPOSIT_IN_PROGRESS)
    }

    @Test
    fun `changePaymentTaskStatusToPaidTask passed correctly`() {
        // Given
        doNothing().`when`(paymentTaskService).updateTaskStatus(testPaymentTaskId, APPROVE_IN_PROGRESS)

        // When
        startPodProcess(CHANGE_TASK_STATUS_TO_PAID)
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).hasPassed(CHANGE_TASK_STATUS_TO_PAID.code)
        verify(paymentTaskService, times(1)).updateTaskStatus(testPaymentTaskId, PAID)
    }

    @Test
    fun `changePaymentTaskStatusToPaidTask fails after 3 retries`() {
        // Given
        whenever(paymentTaskService.updateTaskStatus(testPaymentTaskId, PAID))
                .thenThrow(RuntimeException())

        // When
        startPodProcess(CHANGE_TASK_STATUS_TO_PAID)

        // Then
        assertJobRetry(retryCount = 3, retryMinutes = 5)
        assertThat(processInstance).hasNotPassed(CHANGE_TASK_STATUS_TO_PAID.code)
        verify(paymentTaskService, times(3)).updateTaskStatus(testPaymentTaskId, PAID)
    }
}