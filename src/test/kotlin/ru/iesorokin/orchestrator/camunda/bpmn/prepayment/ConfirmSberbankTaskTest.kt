package ru.iesorokin.orchestrator.camunda.bpmn.prepayment

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Before
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.CONFIRM_SBERBANK_TASK

class ConfirmSberbankTaskTest : PrepaymentCamundaTest() {
    private val extOrderId = "extOrderId"
    private val paymentTaskId = "paymentTaskId"
    private val businessKey = "businessKey"

    @Before
    fun setUp() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET.processName)
                .startBeforeActivity(CONFIRM_SBERBANK_TASK.code)
                .setVariable(EXT_ORDER_ID, extOrderId)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .businessKey(businessKey)
                .execute()
    }

    @Test
    fun `execute should send deposit command to sberbank`() {
        // When
        executeJob(processInstance.processInstanceId)

        //Then
        verify(sberbankDepositService, times(1)).doDeposit(paymentTaskId, extOrderId, businessKey)
        assertThat(processInstance).hasPassed(CONFIRM_SBERBANK_TASK.code)
        rule.endProcess(processInstance)
    }
}
