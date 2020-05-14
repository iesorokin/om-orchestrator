package ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Before
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement

class ChangePaymentTaskStatusTaskTest : PrepaymentCamundaTest() {
    private val paymentTaskId = "12345678"

    @Before
    fun setUp() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .startBeforeActivity(BusinessProcessElement.APPROVE_IN_PROGRESS_TASK.code)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariable(EXT_ORDER_ID, paymentTaskId)
                .execute()
    }

    @Test
    fun `execute should not update status in payment task with 3 retires`() {
        whenever(paymentTaskService.updateTaskStatus(paymentTaskId, PaymentTransactionStatus.APPROVE_IN_PROGRESS))
                .thenThrow(RuntimeException())

        assertJobRetry(retryCount = 3, retryMinutes = 5)
        BpmnAwareAssertions.assertThat(processInstance)
                .hasNotPassed(BusinessProcessElement.APPROVE_IN_PROGRESS_TASK.code)
        verify(paymentTaskService, times(3))
                .updateTaskStatus(paymentTaskId, PaymentTransactionStatus.APPROVE_IN_PROGRESS)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "test end")
    }

    @Test
    fun `execute should change status in payment task with retries`() {
        whenever(paymentTaskService.updateTaskStatus(paymentTaskId, PaymentTransactionStatus.APPROVE_IN_PROGRESS))
                .thenThrow(RuntimeException())
                .thenAnswer {}

        val retries = executeJob(processInstance.processInstanceId)
        Assertions.assertThat(retries).isEqualTo(2)
        assertLockExpirationTime(5)
        executeJob(processInstance.processInstanceId)

        BpmnAwareAssertions.assertThat(processInstance)
                .hasPassed(BusinessProcessElement.APPROVE_IN_PROGRESS_TASK.code)
        verify(paymentTaskService, times(2))
                .updateTaskStatus(paymentTaskId, PaymentTransactionStatus.APPROVE_IN_PROGRESS)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "test end")
    }
}