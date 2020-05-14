package ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement

class EndTheProcessTaskTest : PrepaymentCamundaTest() {

    private val paymentTaskId = "12345678"


    @Test
    fun `endTheProcessTask must end process`() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .startAfterActivity(BusinessProcessElement.PAYMENT_TASK_EXPIRED.code)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(BusinessProcessElement.END_THE_PROCESS_TASK.code)
        BpmnAwareAssertions.assertThat(processInstance).isEnded
    }
}
