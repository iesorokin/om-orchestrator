package ru.iesorokin.orchestrator.camunda.bpmn.refund

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.junit.After
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.refund.base.RefundCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.CURRENT_PAYMENT_STATUS
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement

class SendRefundSmsTaskTest : RefundCamundaTest() {
    private val paymentTaskId = "12345678"
    private val solutionId = "87654321"

    @After
    fun after() {
        rule.endProcess(processInstance)
    }

    @Test
    fun `should success send refund sms task when workflowId is businessKey`() {
        val businessKey = "businessKey"

        startProcess(businessKey)

        BpmnAwareTests.execute(BpmnAwareTests.job(processInstance))
        BpmnAwareAssertions.assertThat(processInstance)
                .hasPassed(BusinessProcessElement.SEND_REFUND_SMS_TASK.code)

        verify(smsService, times(1)).sendMultiSms(paymentTaskId, businessKey, solutionId)
    }

    fun startProcess(businessKey: String?) {
        processInstance = rule.runtimeService.createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .startBeforeActivity(BusinessProcessElement.SEND_REFUND_SMS_TASK.code)
                .businessKey(businessKey)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariable(EXT_ORDER_ID, solutionId)
                .setVariable(CURRENT_PAYMENT_STATUS, "COMPLETED")
                .execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted
    }
}
