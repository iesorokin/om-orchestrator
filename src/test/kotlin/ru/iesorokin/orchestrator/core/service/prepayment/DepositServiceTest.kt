package ru.iesorokin.payment.orchestrator.core.service.prepayment

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import ru.iesorokin.payment.getFileAsObject
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.DepositEventType
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.payment.orchestrator.output.stream.sender.SberbankDepositCommandMessageSender

private const val FILE_PATH = "depositService/"

class DepositServiceTest : BaseSpringBootWithCamundaTest() {

    @MockBean
    private lateinit var sberbankDepositCommandSender: SberbankDepositCommandMessageSender
    @MockBean
    private lateinit var paymentTaskService: PaymentTaskService
    @Autowired
    private lateinit var depositService: DepositService

    val paymentTaskId = "123"
    val orderId = "orderId"

    @Test
    fun `doDeposit correct sum totalAmount and send deposit command`() {
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(objectOf<PaymentTask>("correct"))

        depositService.doDeposit(paymentTaskId, orderId)

        verify(sberbankDepositCommandSender, times(1)).sendDepositComand(orderId, "201.56".toBigDecimal(), 35)
        verifyNoMoreInteractions(sberbankDepositCommandSender)
    }

    @Test(expected = IllegalStateException::class)
    fun `doDeposit should throw IllegalArgumentException if executionStore is null in task`() {
        whenever(paymentTaskService.getPaymentTask(paymentTaskId))
                .thenReturn(objectOf<PaymentTask>("executionStore_is_null"))

        depositService.doDeposit(paymentTaskId, orderId)

        verifyZeroInteractions(sberbankDepositCommandSender)
    }

    @Test
    fun `handleDepositEvent should pass receiveSberbankConfirmationSuccessEvent if SUCCESS`() {
        testWithProcess { process ->
            BpmnAwareAssertions.assertThat(process)
                    .isWaitingAt(BusinessProcessElement.RECEIVE_SBERBANK_CONFIRMATION_SUCCESS_EVENT.code)

            depositService.handleDepositEvent(orderId, DepositEventType.SUCCESS)

            BpmnAwareAssertions.assertThat(process)
                    .hasPassed(BusinessProcessElement.RECEIVE_SBERBANK_CONFIRMATION_SUCCESS_EVENT.code)
            BpmnAwareAssertions.assertThat(process)
                    .hasNotPassed(BusinessProcessElement.RECEIVE_SBERBANK_CONFIRMATION_FAIL_EVENT.code)
        }
    }

    @Test
    fun `handleDepositEvent should pass receiveSberbankConfirmationFailEvent if SUCCESS`() {
        testWithProcess { process ->
            BpmnAwareAssertions.assertThat(process)
                    .isWaitingAt(BusinessProcessElement.RECEIVE_SBERBANK_CONFIRMATION_SUCCESS_EVENT.code)

            depositService.handleDepositEvent(orderId, DepositEventType.FAIL)

            BpmnAwareAssertions.assertThat(process)
                    .hasPassed(BusinessProcessElement.RECEIVE_SBERBANK_CONFIRMATION_FAIL_EVENT.code)
        }
    }

    private fun testWithProcess(test: (process: ProcessInstance) -> Unit) {
        val process = startProcess()
        try {
            test.invoke(process)
        } finally {
            stopProcess(process)
        }
    }

    private fun startProcess(): ProcessInstance {
        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .startBeforeActivity(BusinessProcessElement.RECEIVE_SBERBANK_CONFIRMATION_SUCCESS_EVENT.code)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariable(EXT_ORDER_ID, orderId)
                .setVariable("fullApprove", true)
                .setVariable("confirmSberbankCount", 10)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted

        return processInstance
    }

    private fun stopProcess(processInstance: ProcessInstance) {
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
        BpmnAwareAssertions.assertThat(processInstance).isEnded
    }

    private inline fun <reified T> objectOf(prefix: String): T {
        return getFileAsObject("$FILE_PATH${T::class.simpleName}-$prefix.json")
    }
}
