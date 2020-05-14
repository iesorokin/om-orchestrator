package ru.iesorokin.payment.orchestrator.camunda.bpmn.giveaway

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement

class ProcessTpNetGiveAwayTaskTest : GiveAwayCamundaTest() {

    @Test
    fun `processTpNetGiveAway should call tpNetService`() {
        val paymentTaskIdValue = "paymentTaskIdValue"

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(Process.PAYMENT_GIVEAWAY.processName)
                .setVariable(PAYMENT_TASK_ID, paymentTaskIdValue)
                .startBeforeActivity(BusinessProcessElement.TP_NET_GIVE_AWAY_TASK.code)
                .execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted

        executeJob(processInstance.processInstanceId)
        verify(tpNetService, times(1)).doGiveAway(paymentTaskIdValue)

        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "test end")
    }
}