package ru.iesorokin.orchestrator.camunda.bpmn.tpNetInteraction

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.tpNetInteraction.base.TpNetInteractionCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.tpnetinteraction.INCIDENT_TYPE
import ru.iesorokin.orchestrator.core.domain.TpnetItsmTicket
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.CREATE_ITSM_TICKET_DEPOSIT
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.CREATE_ITSM_TICKET_GIVEAWAY
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.RECEIVE_TP_NET_DEPOSIT_SUCCESS
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.RECEIVE_TP_NET_GIVEAWAY_SUCCESS
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.RECIEVE_TP_NET_SUCCESS_DEPOSIT_TIMER
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.RECIEVE_TP_NET_SUCCESS_GIVEAWAY_TIMER


class CreateTpNetItsmTicketTasksTest : TpNetInteractionCamundaTest() {

    private val deposit = "DEPOSIT"
    private val giveAway = "GIVEAWAY"

    @Test
    fun `should create itsm deposit incident when timer will be executed`() {
        checkItsmIncidentCreation(
                startElement = RECEIVE_TP_NET_DEPOSIT_SUCCESS,
                itsmElement = CREATE_ITSM_TICKET_DEPOSIT,
                timerElement = RECIEVE_TP_NET_SUCCESS_DEPOSIT_TIMER,
                ticketType = deposit
        )
    }

    @Test
    fun `should create itsm giveAway incident when timer will be executed`() {
        checkItsmIncidentCreation(
                startElement = RECEIVE_TP_NET_GIVEAWAY_SUCCESS,
                itsmElement = CREATE_ITSM_TICKET_GIVEAWAY,
                timerElement = RECIEVE_TP_NET_SUCCESS_GIVEAWAY_TIMER,
                ticketType = giveAway
        )
    }

    @Test
    fun `createTpNetGiveAwayItsmTicketTask fails after 3 retries correctly`() {
        // Given
        whenever(itsmService.createTicket(any())).thenThrow(RuntimeException())

        // When
        startProcess(CREATE_ITSM_TICKET_GIVEAWAY, giveAway)

        // Then
        assertJobRetry(retryCount = 3, retryMinutes = 15)
        assertThat(processInstance).hasNotPassed(CREATE_ITSM_TICKET_GIVEAWAY.code)
        rule.endProcess(processInstance)
    }

    @Test
    fun `createTpNetDepositItsmTicketTask fails after 3 retries correctly`() {
        // Given
        whenever(itsmService.createTicket(any())).thenThrow(RuntimeException())

        // When
        startProcess(CREATE_ITSM_TICKET_DEPOSIT, deposit)

        // Then
        assertJobRetry(retryCount = 3, retryMinutes = 15)
        assertThat(processInstance).hasNotPassed(CREATE_ITSM_TICKET_DEPOSIT.code)
        rule.endProcess(processInstance)
    }

    private fun checkItsmIncidentCreation(
            startElement: TpNetInteractionProcessElement,
            itsmElement: TpNetInteractionProcessElement,
            timerElement: TpNetInteractionProcessElement,
            ticketType: String
    ) {
        startProcess(startElement, ticketType)
        assertThat(processInstance).isWaitingAt(startElement.code)
        assertThat(processInstance).isNotWaitingAt(itsmElement.code)

        executeJob(processInstance.processInstanceId, timerElement.code)

        assertThat(processInstance).isWaitingAt(startElement.code)
        assertThat(processInstance).isWaitingAt(itsmElement.code)

        executeJob(processInstance.processInstanceId, itsmElement.code)

        verify(itsmService).createTicket(
                TpnetItsmTicket(
                        paymentTaskId = testPaymentTaskId,
                        processInstanceId = processInstance.processInstanceId,
                        tpnetOperationType = ticketType
                )
        )

        assertThat(processInstance).hasPassed(itsmElement.code)
        assertThat(processInstance).isWaitingAt(startElement.code)
        rule.endProcess(processInstance)
    }

    private fun startProcess(startElement: TpNetInteractionProcessElement, ticketType: String) {
        startTpNetInteractionProcess(startElement, mapOf(INCIDENT_TYPE to ticketType))
    }
}
