package ru.iesorokin.orchestrator.camunda.bpmn.tpNetInteraction

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.tpNetInteraction.base.TpNetInteractionCamundaTest
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.PLACE_TP_NET_DEPOSIT
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.PLACE_TP_NET_GIVEAWAY

class PlaceTpNetTasksTest : TpNetInteractionCamundaTest() {

    @Test
    fun `placeTpNetDepositTask passed correctly`() {
        // When
        startTpNetInteractionProcess(PLACE_TP_NET_DEPOSIT)
        executeJob(processInstance.processInstanceId)

        // Then
        verify(tpNetService, times(1)).doDeposit(testPaymentTaskId)
        assertThat(processInstance).hasPassed(PLACE_TP_NET_DEPOSIT.code)
        rule.endProcess(processInstance)
    }

    @Test
    fun `placeTpNetDepositTask fails after 3 retries correctly`() {
        // Given
        whenever(tpNetService.doDeposit(testPaymentTaskId)).thenThrow(RuntimeException())

        // When
        startTpNetInteractionProcess(PLACE_TP_NET_DEPOSIT)

        // Then
        assertJobRetry(retryCount = 3, retryMinutes = 5)
        assertThat(processInstance).hasNotPassed(PLACE_TP_NET_DEPOSIT.code)
        rule.endProcess(processInstance)
    }

    @Test
    fun `placeTpNetGiveAwayTask passed correctly`() {
        // When
        startTpNetInteractionProcess(PLACE_TP_NET_GIVEAWAY)
        executeJob(processInstance.processInstanceId)

        // Then
        verify(tpNetService, times(1)).doGiveAway(testPaymentTaskId)
        assertThat(processInstance).hasPassed(PLACE_TP_NET_GIVEAWAY.code)
        rule.endProcess(processInstance)
    }

    @Test
    fun `placeTpNetGiveAwayTask fails after 3 retries correctly`() {
        // Given
        whenever(tpNetService.doGiveAway(testPaymentTaskId)).thenThrow(RuntimeException())

        // When
        startTpNetInteractionProcess(PLACE_TP_NET_GIVEAWAY)

        // Then
        assertJobRetry(retryCount = 3, retryMinutes = 5)
        assertThat(processInstance).hasNotPassed(PLACE_TP_NET_GIVEAWAY.code)
        rule.endProcess(processInstance)
    }

}