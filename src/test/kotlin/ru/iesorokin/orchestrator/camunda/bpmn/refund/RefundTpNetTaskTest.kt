package ru.iesorokin.orchestrator.camunda.bpmn.refund

import com.nhaarman.mockito_kotlin.verify
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.After
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.refund.base.RefundCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.RefundProcessElement.REFUND_TP_NET_TASK

class RefundTpNetTaskTest : RefundCamundaTest() {

    private val paymentTaskId = "paymentTaskId"

    @After
    fun after() {
        rule.endProcess(processInstance)
    }

    //@TODO remove this test when all process instances on production without businessKey will be ended
    @Test
    fun `execute should do refund when workflowId is processInstanceId`() {
        startProcess(null)

        verify(refundTpNetService).doRefund(
                refundWorkflowId = processInstance.processInstanceId,
                paymentTaskId = paymentTaskId
        )
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(REFUND_TP_NET_TASK.code)
    }

    @Test
    fun `execute should do refund when workflowId is businessKey`() {
        val businessKey = "businessKey"
        startProcess(businessKey)

        verify(refundTpNetService).doRefund(
                refundWorkflowId = businessKey,
                paymentTaskId = paymentTaskId
        )
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(REFUND_TP_NET_TASK.code)
    }

    private fun startProcess(businessKey: String?) {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .startBeforeActivity(REFUND_TP_NET_TASK.code)
                .businessKey(businessKey)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .execute()
    }
}
