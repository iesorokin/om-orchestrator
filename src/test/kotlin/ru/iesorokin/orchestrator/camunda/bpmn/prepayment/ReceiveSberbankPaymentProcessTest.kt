package ru.iesorokin.orchestrator.camunda.bpmn.prepayment

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Before
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement
import kotlin.test.assertTrue

class ReceiveSberbankPaymentProcessTest : PrepaymentCamundaTest() {

    private val extOrderId = "87654321"

    @Before
    fun preparation() {
        val paymentTaskId = "12345678"

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .startAfterActivity(BusinessProcessElement.SAVE_WORKFLOW_ID_TASK.code)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariable(EXT_ORDER_ID, extOrderId)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted
        val activeSubscriptions = getActiveSubscriptions(processInstance.processInstanceId)
        assertTrue(activeSubscriptions.toString()) {
            activeSubscriptions.containsAll(
                    listOf(
                            BusinessProcessEvent.PAYMENT_RECEIVED.message,
                            BusinessProcessEvent.PAYMENT_EXPIRED.message
                    )
            )
        }
    }

    @Test
    fun `should success receive payment`() {
        BpmnAwareAssertions.assertThat(processInstance).isWaitingAt(BusinessProcessElement.RECEIVE_PAYMENT_TASK.code)

        rule.runtimeService
                .createMessageCorrelation(BusinessProcessEvent.PAYMENT_RECEIVED.message)
                .processInstanceId(processInstance.processInstanceId)
                .correlateWithResult()


        BpmnAwareAssertions.assertThat(processInstance).isNotEnded
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }

    @Test
    fun `should expire receive payment`() {
        BpmnAwareAssertions.assertThat(processInstance).isWaitingAt(BusinessProcessElement.RECEIVE_PAYMENT_TASK.code)

        rule.runtimeService
                .createMessageCorrelation(BusinessProcessEvent.PAYMENT_EXPIRED.message)
                .processInstanceId(processInstance.processInstanceId)
                .correlateWithResult()


        BpmnAwareAssertions.assertThat(processInstance).hasPassed(BusinessProcessElement.RECEIVE_PAYMENT_TASK.code)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }

}
