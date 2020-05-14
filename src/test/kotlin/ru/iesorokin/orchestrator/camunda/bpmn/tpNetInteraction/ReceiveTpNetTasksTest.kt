package ru.iesorokin.payment.orchestrator.camunda.bpmn.tpNetInteraction

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.camunda.bpmn.tpNetInteraction.base.TpNetInteractionCamundaTest
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.TPNET_DEPOSIT_FAIL
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.TPNET_DEPOSIT_SUCCESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.TPNET_GIVE_AWAY_FAIL
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.TPNET_GIVE_AWAY_SUCCESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.RECEIVE_TP_NET_DEPOSIT_SUCCESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.RECEIVE_TP_NET_GIVEAWAY_SUCCESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.SOLVE_ISSUE_PLACE_DEPOSIT
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.SOLVE_ISSUE_PLACE_GIVEAWAY
import kotlin.test.assertTrue

class ReceiveTpNetTasksTest : TpNetInteractionCamundaTest() {

    @Test
    fun `receiveTpNetDepositSuccess correlated correctly`() {
        checkPositiveScenario(
                startElement = RECEIVE_TP_NET_DEPOSIT_SUCCESS,
                message = TPNET_DEPOSIT_SUCCESS.message,
                waitReceiverList = listOf(TPNET_DEPOSIT_SUCCESS.message, TPNET_DEPOSIT_FAIL.message)
        )
    }

    @Test
    fun `receiveTpNetDepositFail correlated correctly`() {
        checkFailScenario(
                startElement = RECEIVE_TP_NET_DEPOSIT_SUCCESS,
                waitingAtElement = SOLVE_ISSUE_PLACE_DEPOSIT,
                message = TPNET_DEPOSIT_FAIL.message,
                waitReceiverList = listOf(TPNET_DEPOSIT_SUCCESS.message, TPNET_DEPOSIT_FAIL.message)
        )
    }

    @Test
    fun `receiveTpNetGiveAwaySuccess correlated correctly`() {
        checkPositiveScenario(
                startElement = RECEIVE_TP_NET_GIVEAWAY_SUCCESS,
                message = TPNET_GIVE_AWAY_SUCCESS.message,
                waitReceiverList = listOf(TPNET_GIVE_AWAY_SUCCESS.message, TPNET_GIVE_AWAY_FAIL.message)
        )
    }

    @Test
    fun `receiveTpNetGiveAwayFail correlated correctly`() {
        checkFailScenario(
                startElement = RECEIVE_TP_NET_GIVEAWAY_SUCCESS,
                waitingAtElement = SOLVE_ISSUE_PLACE_GIVEAWAY,
                message = TPNET_GIVE_AWAY_FAIL.message,
                waitReceiverList = listOf(TPNET_GIVE_AWAY_SUCCESS.message, TPNET_GIVE_AWAY_FAIL.message)
        )
    }

    private fun checkPositiveScenario(startElement: TpNetInteractionProcessElement, message: String, waitReceiverList: Collection<String>) {
        // Given
        startProcess(startElement, waitReceiverList)

        // When
        rule.runtimeService
                .createMessageCorrelation(message)
                .processInstanceId(processInstance.processInstanceId)
                .correlate()
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).isStarted
        assertThat(processInstance).hasPassed(startElement.code)
    }

    private fun checkFailScenario(startElement: TpNetInteractionProcessElement, waitingAtElement: TpNetInteractionProcessElement, message: String, waitReceiverList: Collection<String>) {
        // Given
        startProcess(startElement, waitReceiverList)

        // When
        rule.runtimeService
                .createMessageCorrelation(message)
                .processInstanceId(processInstance.processInstanceId)
                .correlateWithResult()
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).isStarted
        assertThat(processInstance).hasPassed(startElement.code)
        assertThat(processInstance).isWaitingAt(waitingAtElement.code)
        rule.endProcess(processInstance)
    }

    private fun startProcess(startElement: TpNetInteractionProcessElement, waitReceiverList: Collection<String>) {
        startTpNetInteractionProcess(startElement)

        val activeSubscriptions = getActiveSubscriptions(processInstance.processInstanceId)
        assertTrue { activeSubscriptions.containsAll(waitReceiverList) }
    }

}