package ru.iesorokin.payment.orchestrator.camunda.bpmn.pod

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.base.PodCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.EXECUTION_STORE
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.GIVE_AWAY
import ru.iesorokin.payment.orchestrator.core.constants.process.GIVE_AWAY_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement
import kotlin.test.assertEquals

class RunGiveAwayProcessActivityTest : PodCamundaTest() {

    private val testGiveAwayId = "34567890"
    private val testExecutionStore = 59

    @Test
    fun `sendFinishStatusToSolutionTask passed correctly`() {
        //Given
        startPodProcess(PodProcessElement.RUN_GIVEAWAY_PROCESS_ACTIVITY,
                mapOf(GIVE_AWAY_ID to testGiveAwayId, EXECUTION_STORE to testExecutionStore)
        )

        // When
        executeJob(processInstance.processInstanceId)

        // Then
        val giveAwayProcessInstanceAssert = BpmnAwareAssertions.assertThat(processInstance).calledProcessInstance()
        giveAwayProcessInstanceAssert.isActive
        giveAwayProcessInstanceAssert.variables()
                .containsEntry(GIVE_AWAY_ID, testGiveAwayId)
                .containsEntry(EXECUTION_STORE, testExecutionStore)
                .containsEntry(PAYMENT_TASK_ID, testPaymentTaskId)
                .containsEntry(EXT_ORDER_ID, testSolutionId)
        giveAwayProcessInstanceAssert.hasProcessDefinitionKey(GIVE_AWAY)
        assertEquals(testGiveAwayId, giveAwayProcessInstanceAssert.actual.businessKey)
        rule.endProcess(processInstance)
    }

}