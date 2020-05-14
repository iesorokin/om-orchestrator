package ru.iesorokin.orchestrator.camunda.bpmn.prepayment

import org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat
import org.junit.Before
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent.CANCEL_PREPAYMENT_PROCESS
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.CANCEL_PREPAYMENT_TASK
import kotlin.test.assertTrue

class ReceiveProcessCancellationMessageTest : PrepaymentCamundaTest() {

    @Before
    fun preparation() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .setVariable(PAYMENT_TASK_ID, "3710892")
                .execute()
        assertThat(processInstance).isStarted
        val activeSubscriptions = getActiveSubscriptions(processInstance.processInstanceId)
        assertTrue(activeSubscriptions.toString()) {
            activeSubscriptions.containsAll(listOf(
                    CANCEL_PREPAYMENT_PROCESS.message))
        }
    }

    @Test
    fun `should receive cancellation message`() {
        rule.runtimeService.createMessageCorrelation(CANCEL_PREPAYMENT_PROCESS.message)
                .processInstanceId(processInstance.processInstanceId)
                .correlateWithResult()
        executeJob(processInstance.processInstanceId, CANCEL_PREPAYMENT_TASK.code)

        assertThat(processInstance).hasPassed(CANCEL_PREPAYMENT_TASK.code)
        assertThat(processInstance).hasPassedInOrder(CANCEL_PREPAYMENT_TASK.code)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }
}
