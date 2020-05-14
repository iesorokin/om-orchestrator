package ru.iesorokin.orchestrator.camunda.bpmn.prepayment

import com.nhaarman.mockito_kotlin.only
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Before
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement

class StartRefundProcessTaskTest : PrepaymentCamundaTest() {
    private val paymentTaskId = "12345678"

    @Before
    fun setUp() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .startBeforeActivity(BusinessProcessElement.REFUND_PROCESS.code)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted
    }

    @Test
    fun `execute should start refund process`() {
        //Given
        whenever(refundService.startRefundProcess(paymentTaskId)).then {}

        //Then
        executeJob(processInstance.processInstanceId)
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(BusinessProcessElement.REFUND_PROCESS.code)
        verify(refundService, only()).startRefundProcess(paymentTaskId)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "test end")
    }

}
