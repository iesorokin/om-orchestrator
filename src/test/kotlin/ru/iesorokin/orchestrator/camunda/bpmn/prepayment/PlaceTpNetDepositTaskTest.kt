package ru.iesorokin.orchestrator.camunda.bpmn.prepayment

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Before
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.PLACE_TPNET_DEPOSIT_TASK

class PlaceTpNetDepositTaskTest : PrepaymentCamundaTest() {
    private val paymentTaskId = "paymentTaskId"

    @Before
    fun setUp() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET.processName)
                .startBeforeActivity(PLACE_TPNET_DEPOSIT_TASK.code)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .execute()
    }

    @Test
    fun `execute should send deposit command to tpNet`() {
        // When
        executeJob(processInstance.processInstanceId)

        // Then
        verify(tpNetService, times(1)).doDeposit(paymentTaskId)
        assertThat(processInstance).hasPassed(PLACE_TPNET_DEPOSIT_TASK.code)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }

}
