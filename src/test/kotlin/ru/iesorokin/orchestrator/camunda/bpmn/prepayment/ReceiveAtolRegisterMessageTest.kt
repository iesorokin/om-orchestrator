package ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment

import org.camunda.bpm.engine.test.assertions.ProcessEngineTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Before
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.RECEIVE_ATOL_SUCCESS_RECEIVE_TASK
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.SAVE_DATA_FROM_ATOL_TASK
import kotlin.test.assertTrue

class ReceiveAtolRegisterMessageTest : PrepaymentCamundaTest() {

    @Before
    fun preparation() {
        processInstance = rule.runtimeService.createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .startBeforeActivity(RECEIVE_ATOL_SUCCESS_RECEIVE_TASK.code)
                .execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted
        val activeSubscriptions = getActiveSubscriptions(processInstance.processInstanceId)
        assertTrue(activeSubscriptions.toString()) {
            activeSubscriptions.containsAll(listOf(
                    BusinessProcessEvent.ATOL_REGISTER_SUCCESS.message))
        }
    }

    @Test
    fun `should success receive atol registration`() {


        rule.runtimeService.createMessageCorrelation(BusinessProcessEvent.ATOL_REGISTER_SUCCESS.message)
                .processInstanceId(processInstance.processInstanceId)
                .correlateWithResult()

        BpmnAwareAssertions.assertThat(processInstance).isStarted
        executeJob(processInstance.processInstanceId)
        ProcessEngineTests.assertThat(processInstance).hasPassedInOrder(RECEIVE_ATOL_SUCCESS_RECEIVE_TASK.code)
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(RECEIVE_ATOL_SUCCESS_RECEIVE_TASK.code)
        BpmnAwareAssertions.assertThat(processInstance).isWaitingAt(SAVE_DATA_FROM_ATOL_TASK.code)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }


}
