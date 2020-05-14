package ru.iesorokin.orchestrator.camunda.bpmn.podAgent

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Before
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.podAgent.base.PodAgentCamundaTest
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent.BILLING_PAID_STATUS
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.POD_AGENT
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement.CHANGE_TASK_STATUS_TO_DEPOSIT_GIVEAWAY_DONE_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement.CHANGE_TASK_STATUS_TO_PAID_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodAgentProcessElement.RECEIVE_PAID_STATUS_TASK
import kotlin.test.assertTrue

class ReceivePaidStatusTest : PodAgentCamundaTest() {

    @Before
    fun preparation() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(POD_AGENT.processName)
                .startAfterActivity(CHANGE_TASK_STATUS_TO_DEPOSIT_GIVEAWAY_DONE_TASK.code)
                .execute()
        assertThat(processInstance).isStarted
        assertThat(processInstance).isWaitingAt(RECEIVE_PAID_STATUS_TASK.code)

        val activeSubscriptions = getActiveSubscriptions(processInstance.processInstanceId)
        assertTrue(activeSubscriptions.toString()) {
            activeSubscriptions.containsAll(listOf(
                    BILLING_PAID_STATUS.message
            ))
        }
    }

    @Test
    fun `receivePaidStatusTask correlated correctly`() {
        // When
        rule.runtimeService
                .createMessageCorrelation(BILLING_PAID_STATUS.message)
                .processInstanceId(processInstance.processInstanceId)
                .correlateWithResult()
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).isStarted
        assertThat(processInstance).hasPassed(RECEIVE_PAID_STATUS_TASK.code)
        assertThat(processInstance).isWaitingAt(CHANGE_TASK_STATUS_TO_PAID_TASK.code)
        rule.endProcess(processInstance)
    }

}