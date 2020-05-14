package ru.iesorokin.payment.orchestrator.camunda.bpmn.pod

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Before
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.base.PodCamundaTest
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.ATOL_REGISTER_SUCCESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement.RECEIVE_ATOL_SUCCESS
import kotlin.test.assertTrue

class ReceiveAtolSuccessTaskTest : PodCamundaTest() {

    @Before
    fun preparation() {
        startPodProcess(RECEIVE_ATOL_SUCCESS)
        assertThat(processInstance).isStarted

        val activeSubscriptions = getActiveSubscriptions(processInstance.processInstanceId)
        assertTrue(activeSubscriptions.toString()) {
            activeSubscriptions.containsAll(listOf(ATOL_REGISTER_SUCCESS.message))
        }
    }

    @Test
    fun `receiveAtolSuccess correlated correctly`() {
        // When
        rule.runtimeService
                .createMessageCorrelation(ATOL_REGISTER_SUCCESS.message)
                .processInstanceId(processInstance.processInstanceId)
                .correlateWithResult()
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).isStarted
        assertThat(processInstance).hasPassed(PodProcessElement.RECEIVE_ATOL_SUCCESS.code)
        assertThat(processInstance).isWaitingAt(PodProcessElement.SAVE_DATA_FROM_ATOL.code)
        rule.endProcess(processInstance)
    }

}