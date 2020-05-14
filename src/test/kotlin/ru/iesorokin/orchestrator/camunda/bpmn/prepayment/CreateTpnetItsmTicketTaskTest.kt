package ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment

import com.nhaarman.mockito_kotlin.verify
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Before
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.TP_NET_OPERATION_TYPE
import ru.iesorokin.payment.orchestrator.core.domain.TpnetItsmTicket
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement


class CreateTpnetItsmTicketTaskTest : PrepaymentCamundaTest() {
    private val extOrderId = "extOrderId"
    private val paymentTaskId = "paymentTaskId"
    private val tpnetOperationType = "DEPOSIT"

    @Before
    fun setUp() {
        processInstance = rule.runtimeService
            .createProcessInstanceByKey(Process.SBERBANK_PREPAYMENT_WITH_TPNET.processName)
            .startBeforeActivity(BusinessProcessElement.RECEIVE_TPNET_DEPOSIT_SUCCESS_EVENT.code)
            .setVariable(EXT_ORDER_ID, extOrderId)
            .setVariable(PAYMENT_TASK_ID, paymentTaskId)
            .setVariableLocal(TP_NET_OPERATION_TYPE, tpnetOperationType)
            .execute()
    }


    @Test
    fun `should create itsm incident after timer`() {
        BpmnAwareAssertions.assertThat(processInstance).isWaitingAt(BusinessProcessElement.RECEIVE_TPNET_DEPOSIT_SUCCESS_EVENT.code)
        BpmnAwareAssertions.assertThat(processInstance).isNotWaitingAt(BusinessProcessElement.CREATE_TPNET_ITSM_TICKET_TASK.code)

        executeJob(processInstance.processInstanceId, "receiveTpNetSuccessTimer")

        BpmnAwareAssertions.assertThat(processInstance).isWaitingAt(BusinessProcessElement.RECEIVE_TPNET_DEPOSIT_SUCCESS_EVENT.code)
        BpmnAwareAssertions.assertThat(processInstance).isWaitingAt(BusinessProcessElement.CREATE_TPNET_ITSM_TICKET_TASK.code)
        executeJob(processInstance.processInstanceId)

        verify(itsmService).createTicket(
            TpnetItsmTicket(
                    paymentTaskId = paymentTaskId,
                    processInstanceId = processInstance.processInstanceId,
                    tpnetOperationType = tpnetOperationType
            )
        )

        BpmnAwareAssertions.assertThat(processInstance).hasPassed(BusinessProcessElement.CREATE_TPNET_ITSM_TICKET_TASK.code)
        BpmnAwareAssertions.assertThat(processInstance).isWaitingAt(BusinessProcessElement.RECEIVE_TPNET_DEPOSIT_SUCCESS_EVENT.code)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "test end")
    }
}
