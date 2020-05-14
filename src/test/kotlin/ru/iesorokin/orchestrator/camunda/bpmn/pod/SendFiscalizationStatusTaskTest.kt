package ru.iesorokin.orchestrator.camunda.bpmn.pod

import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Test
import org.mockito.Mockito.times
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.pod.base.PodCamundaTest
import ru.iesorokin.orchestrator.core.enums.FiscalizationStatus
import ru.iesorokin.orchestrator.core.enums.FiscalizationStatus.FISCALIZATION_FINISHED
import ru.iesorokin.orchestrator.core.enums.FiscalizationStatus.FISCALIZATION_STARTED
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodProcessElement
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodProcessElement.SEND_FINISHED_FISCALIZATION_STATUS_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodProcessElement.SEND_STARTED_FISCALIZATION_STATUS_TASK

class SendFiscalizationStatusTaskTest : PodCamundaTest() {

    @Test
    fun `sendStartedFiscalizationStatusTask passed correctly`() {
        sendFiscalizationStatusTaskTest(SEND_STARTED_FISCALIZATION_STATUS_TASK, FISCALIZATION_STARTED)
    }

    @Test
    fun `sendFinishedFiscalizationStatusTask passed correctly`() {
        sendFiscalizationStatusTaskTest(SEND_FINISHED_FISCALIZATION_STATUS_TASK, FISCALIZATION_FINISHED)
    }

    @Test
    fun `sendStartedFiscalizationStatusTask fails after 3 retries`() {
        sendFiscalizationStatusTaskRetryTest(SEND_STARTED_FISCALIZATION_STATUS_TASK, FISCALIZATION_STARTED)
    }

    @Test
    fun `sendFinishedFiscalizationStatusTask fails after 3 retries`() {
        sendFiscalizationStatusTaskRetryTest(SEND_FINISHED_FISCALIZATION_STATUS_TASK, FISCALIZATION_FINISHED)
    }


    private fun sendFiscalizationStatusTaskTest(processElement: PodProcessElement, status: FiscalizationStatus) {
        // Given
        doNothing().`when`(solutionService).sendFiscalizationStatus(testPaymentTaskId, testSolutionId, FiscalizationStatus.valueOf(status.name))
        startPodProcess(processElement)

        // When
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).hasPassed(processElement.code)
        verify(solutionService, times(1)).sendFiscalizationStatus(testPaymentTaskId, testSolutionId, FiscalizationStatus.valueOf(status.name))
        rule.endProcess(processInstance)
    }

    private fun sendFiscalizationStatusTaskRetryTest(processElement: PodProcessElement, status: FiscalizationStatus) {
        //Given
        whenever(solutionService.sendFiscalizationStatus(testPaymentTaskId, testSolutionId, FiscalizationStatus.valueOf(status.name)))
                .thenThrow(RuntimeException())

        // When
        startPodProcess(processElement)

        // Then
        assertJobRetry(retryCount = 3, retryMinutes = 5)
        assertThat(processInstance).hasNotPassed(processElement.code)
        rule.endProcess(processInstance)
    }

}