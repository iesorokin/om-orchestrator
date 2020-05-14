package ru.iesorokin.payment.orchestrator.core.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.DepositEventType
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement
import ru.iesorokin.payment.orchestrator.core.task.common.ChangePaymentTaskStatusTask
import ru.iesorokin.payment.orchestrator.output.stream.sender.TpNetSender

class TpNetServiceTestWithCamunda : BaseSpringBootWithCamundaTest() {
    @MockBean
    private lateinit var tpNetSender: TpNetSender
    @MockBean
    private lateinit var changePaymentTaskStatusTask: ChangePaymentTaskStatusTask
    @Autowired
    private lateinit var tpNetService: TpNetService

    private val paymentTaskId = "paymentTaskId"

    @Test
    fun `doDeposit should send deposit command meesage`() {
        tpNetService.doDeposit(paymentTaskId)

        verify(tpNetSender).sendTpNetDepositCommandMessage(paymentTaskId)
    }

    @Test
    fun `handleDepositEvent should pass SBERBANK_PREPAYMENT_WITH_TPNET receiveTpNetDepositSuccessEvent if SUCCESS`() {

        doNothing().whenever(changePaymentTaskStatusTask).execute(any())
        testPrepaymentProcess { process ->
            BpmnAwareAssertions.assertThat(process)
                    .isWaitingAt(BusinessProcessElement.RECEIVE_TPNET_DEPOSIT_SUCCESS_EVENT.code)

            tpNetService.handleDepositEvent(paymentTaskId, DepositEventType.SUCCESS)

            BpmnAwareAssertions.assertThat(process)
                    .hasPassed(BusinessProcessElement.RECEIVE_TPNET_DEPOSIT_SUCCESS_EVENT.code)
            BpmnAwareAssertions.assertThat(process)
                    .hasNotPassed(BusinessProcessElement.RECEIVE_TPNET_DEPOSIT_FAIL_EVENT.code)
        }
    }

    @Test
    fun `handleDepositEvent should pass SBERBANK_PREPAYMENT_WITH_TPNET receiveTpNetDepositSuccessEvent if FAIL`() {
        testPrepaymentProcess { process ->
            BpmnAwareAssertions.assertThat(process)
                    .isWaitingAt(BusinessProcessElement.RECEIVE_TPNET_DEPOSIT_SUCCESS_EVENT.code)

            tpNetService.handleDepositEvent(paymentTaskId, DepositEventType.FAIL)

            BpmnAwareAssertions.assertThat(process)
                    .hasPassed(BusinessProcessElement.RECEIVE_TPNET_DEPOSIT_FAIL_EVENT.code)
        }
    }

    @Test
    fun `handleDepositEvent should pass POD_PAYMENT receiveTpNetDepositSuccessEvent if SUCCESS`() {

        testPodProcess { process ->
            BpmnAwareAssertions.assertThat(process)
                    .isWaitingAt(PodProcessElement.RECEIVE_TP_NET_DEPOSIT_SUCCESS.code)

            tpNetService.handleDepositEvent(paymentTaskId, DepositEventType.SUCCESS)

            BpmnAwareAssertions.assertThat(process)
                    .hasPassed(PodProcessElement.RECEIVE_TP_NET_DEPOSIT_SUCCESS.code)
            BpmnAwareAssertions.assertThat(process)
                    .hasNotPassed(PodProcessElement.RECEIVE_TP_NET_DEPOSIT_FAIL.code)
        }
    }

    @Test
    fun `handleDepositEvent should pass POD_PAYMENT receiveTpNetDepositSuccessEvent if FAIL`() {
        testPodProcess { process ->
            BpmnAwareAssertions.assertThat(process)
                    .isWaitingAt(PodProcessElement.RECEIVE_TP_NET_DEPOSIT_SUCCESS.code)

            tpNetService.handleDepositEvent(paymentTaskId, DepositEventType.FAIL)

            BpmnAwareAssertions.assertThat(process)
                    .hasPassed(PodProcessElement.RECEIVE_TP_NET_DEPOSIT_FAIL.code)
        }
    }

    private fun testPrepaymentProcess(test: (process: ProcessInstance) -> Unit) {
        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(Process.SBERBANK_PREPAYMENT_WITH_TPNET.processName)
                .startBeforeActivity(BusinessProcessElement.RECEIVE_TPNET_DEPOSIT_SUCCESS_EVENT.code)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted

        try {
            test.invoke(processInstance)
        } finally {
            rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
            BpmnAwareAssertions.assertThat(processInstance).isEnded
        }
    }

    private fun testPodProcess(test: (process: ProcessInstance) -> Unit) {
        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(Process.POD_PAYMENT.processName)
                .startBeforeActivity(PodProcessElement.RECEIVE_TP_NET_DEPOSIT_SUCCESS.code)
                .businessKey(paymentTaskId)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted

        try {
            test.invoke(processInstance)
        } finally {
            rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
            BpmnAwareAssertions.assertThat(processInstance).isEnded
        }
    }
}
