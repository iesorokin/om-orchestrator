package ru.iesorokin.orchestrator.camunda.bpmn.podAgent

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.After
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.podAgent.base.PodAgentCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.GIVE_AWAY_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.constants.process.tpnetinteraction.INCIDENT_TYPE
import ru.iesorokin.orchestrator.core.constants.process.tpnetinteraction.INCIDENT_TYPE_DEPOSIT
import ru.iesorokin.orchestrator.core.constants.process.tpnetinteraction.INCIDENT_TYPE_GIVEAWAY
import ru.iesorokin.orchestrator.core.constants.process.tpnetinteraction.TP_NET_TYPE
import ru.iesorokin.orchestrator.core.constants.process.tpnetinteraction.TP_NET_TYPE_DEPOSIT
import ru.iesorokin.orchestrator.core.constants.process.tpnetinteraction.TP_NET_TYPE_GIVEAWAY
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.TP_NET_INTERACTION
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement.TPNET_DEPOSIT_CALL_ACTIVITY
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement.TPNET_GIVEAWAY_CALL_ACTIVITY
import kotlin.test.assertEquals

class RunTpNetInteractionProcessActivitiesTest : PodAgentCamundaTest() {

    private val testGiveAwayId = "34567890"

    @After
    fun after() {
        rule.endProcess(processInstance)
    }

    @Test
    fun `tpNetDepositCallActivity passed correctly`() {
        //Given
        startPodAgentProcess(TPNET_DEPOSIT_CALL_ACTIVITY)

        // When
        executeJob(processInstance.processInstanceId)

        // Then
        val depositProcessInstanceAssert = BpmnAwareAssertions.assertThat(processInstance).calledProcessInstance()
        depositProcessInstanceAssert.isActive
        depositProcessInstanceAssert.variables()
                .containsEntry(PAYMENT_TASK_ID, testPaymentTaskId)
                .containsEntry(TP_NET_TYPE, TP_NET_TYPE_DEPOSIT)
                .containsEntry(INCIDENT_TYPE, INCIDENT_TYPE_DEPOSIT)
        depositProcessInstanceAssert.hasProcessDefinitionKey(TP_NET_INTERACTION.processName)
        assertEquals(testPaymentTaskId, depositProcessInstanceAssert.actual.businessKey)
    }

    @Test
    fun `tpNetGiveAwayCallActivity passed correctly`() {
        //Given
        startPodAgentProcess(TPNET_GIVEAWAY_CALL_ACTIVITY, mapOf(GIVE_AWAY_ID to testGiveAwayId))

        // When
        executeJob(processInstance.processInstanceId)

        // Then
        val giveAwayProcessInstanceAssert = BpmnAwareAssertions.assertThat(processInstance).calledProcessInstance()
        giveAwayProcessInstanceAssert.isActive
        giveAwayProcessInstanceAssert.variables()
                .containsEntry(PAYMENT_TASK_ID, testPaymentTaskId)
                .containsEntry(TP_NET_TYPE, TP_NET_TYPE_GIVEAWAY)
                .containsEntry(INCIDENT_TYPE, INCIDENT_TYPE_GIVEAWAY)
        giveAwayProcessInstanceAssert.hasProcessDefinitionKey(TP_NET_INTERACTION.processName)
        assertEquals(testGiveAwayId, giveAwayProcessInstanceAssert.actual.businessKey)
    }

}