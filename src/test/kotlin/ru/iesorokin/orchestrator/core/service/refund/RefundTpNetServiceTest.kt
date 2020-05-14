package ru.iesorokin.payment.orchestrator.core.service.refund

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskFiscalData
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.RefundEventType
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.RefundEventType.FAIL
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.RefundProcessElement.RECEIVE_TP_NET_REFUND_FAIL_TASK
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.RefundProcessElement.RECEIVE_TP_NET_REFUND_SUCCESS_TASK
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.payment.orchestrator.output.stream.sender.TpNetSender
import java.time.ZonedDateTime

class RefundTpNetServiceTest : BaseSpringBootWithCamundaTest() {
    @MockBean
    private lateinit var tpNetSender: TpNetSender
    @MockBean
    private lateinit var paymentTaskService: PaymentTaskService
    @Autowired
    private lateinit var refundTpNetsService: RefundTpNetService

    private val paymentTaskId = "paymentTaskId"
    private val workflowId = "workflowId"

    @Test
    fun `doRefund should send refund command`() {
        refundTpNetsService.doRefund(paymentTaskId = paymentTaskId, refundWorkflowId = workflowId)

        verify(tpNetSender).sendTpNetRefundCommandMessage(paymentTaskId, workflowId)
    }

    @Test
    fun `handleRefundEvent should pass receiveTpNetDepositSuccessEvent if SUCCESS and correlataion was found by businessKey`() {
        val businessKey = "businessKey"
        val processInstance = startProcessRefund(businessKey)
        val refundedTask =  paymentTask(businessKey)
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(refundedTask)

        //When
        refundTpNetsService.handleRefundEvent(paymentTaskId, RefundEventType.SUCCESS)

        //Then
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(RECEIVE_TP_NET_REFUND_SUCCESS_TASK.code)
        BpmnAwareAssertions.assertThat(processInstance).hasNotPassed(RECEIVE_TP_NET_REFUND_FAIL_TASK.code)

        rule.endProcess(processInstance)
    }

    @Test
    fun `handleRefundEvent should pass receiveTpNetDepositSuccessEvent if SUCCESS correlataion was found by processInstanceId`() {
        val processInstance = startProcessRefund()
        val refundedTask =  paymentTask(processInstance.processInstanceId)

        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(refundedTask)
        BpmnAwareAssertions.assertThat(processInstance).isWaitingAt(RECEIVE_TP_NET_REFUND_SUCCESS_TASK.code)

        //When
        refundTpNetsService.handleRefundEvent(paymentTaskId, RefundEventType.SUCCESS)

        //Then
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(RECEIVE_TP_NET_REFUND_SUCCESS_TASK.code)
        BpmnAwareAssertions.assertThat(processInstance).hasNotPassed(RECEIVE_TP_NET_REFUND_FAIL_TASK.code)

        rule.endProcess(processInstance)
    }

    @Test
    fun `handleRefundEvent should pass receiveTpNetDepositSuccessEvent if FAIL`() {
        val processInstance = startProcessRefund()
        val refundedTask =  paymentTask(processInstance.processInstanceId)
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(refundedTask)

        //When
        refundTpNetsService.handleRefundEvent(paymentTaskId, FAIL)

        //Then
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(RECEIVE_TP_NET_REFUND_FAIL_TASK.code)

        rule.endProcess(processInstance)
    }

    private fun startProcessRefund(businessKey: String? = null): ProcessInstance {
        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .startBeforeActivity(RECEIVE_TP_NET_REFUND_SUCCESS_TASK.code)
                .businessKey(businessKey)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted
        BpmnAwareAssertions.assertThat(processInstance).isWaitingAt(RECEIVE_TP_NET_REFUND_SUCCESS_TASK.code)

        return processInstance
    }

    private fun paymentTask(refundWorkflowId: String): PaymentTask =
            PaymentTask(
                    taskId = paymentTaskId,
                    taskStatus = "REFUND",
                    taskType = "TP_NET_TASK_TYPE",
                    lines = listOf(),
                    refundStatusList = listOf(
                            PaymentTaskFiscalData(
                                    refundWorkflowId = refundWorkflowId,
                                    created = ZonedDateTime.now().minusMinutes(5)
                            ),
                            PaymentTaskFiscalData(
                                    refundWorkflowId = "someRefundWorkflowId",
                                    created = ZonedDateTime.now().minusMinutes(10)
                            )
                    )
            )
}

