package ru.iesorokin.orchestrator.camunda.bpmn.refund

import com.nhaarman.mockito_kotlin.verify
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.After
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.refund.base.RefundCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.RefundProcessElement.REFUND_SBERBANK_PAYMENT_TASK

class RefundSberbankPaymentTaskTest : RefundCamundaTest() {

    private val paymentTaskId = "paymentTaskId"
    private val orderId = "orderId"

    @After
    fun after() {
        rule.endProcess(processInstance)
    }

    @Test
    fun `execute should do refund when workflowId is businessKey`() {
        val businessKey = "businessKey"
        startProcess(businessKey)

        executeJob(processInstance.processInstanceId)
        verify(refundSberbankService).doRefund(
                workflowId = businessKey,
                paymentTaskId = paymentTaskId,
                orderId = orderId
        )
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(REFUND_SBERBANK_PAYMENT_TASK.code)
    }

    private fun startProcess(businessKey: String?) {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .startBeforeActivity(REFUND_SBERBANK_PAYMENT_TASK.code)
                .businessKey(businessKey)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariable(EXT_ORDER_ID, orderId)
                .execute()
    }
}
