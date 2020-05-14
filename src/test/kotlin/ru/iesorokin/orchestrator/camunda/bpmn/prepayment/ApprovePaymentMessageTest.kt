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

class ApprovePaymentMessageTest : PrepaymentCamundaTest() {

    private val paymentTaskId = "paymentTaskId"
    private val extOrderId = "extOrderId"

    @Before
    fun setUp() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .startBeforeActivity(BusinessProcessElement.APPROVE_PAYMENT_TASK.code)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariable(EXT_ORDER_ID, extOrderId)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted

        BpmnAwareAssertions.assertThat(processInstance)
                .isWaitingAt(BusinessProcessElement.APPROVE_PAYMENT_TASK.code)
    }

    @Test
    fun `should pass approve payment`() {
        rule.runtimeService
                .createMessageCorrelation(BusinessProcessEvent.PAYMENT_APPROVED.message)
                .processInstanceId(processInstance.processInstanceId)
                .correlateWithResult()

        executeJob(processInstance.processInstanceId)
        BpmnAwareAssertions.assertThat(processInstance).isNotEnded
        BpmnAwareAssertions.assertThat(processInstance)
                .isNotWaitingAt(BusinessProcessElement.APPROVE_PAYMENT_TASK.code)
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(BusinessProcessElement.APPROVE_PAYMENT_TASK.code)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
        BpmnAwareAssertions.assertThat(processInstance).isEnded
    }
}
