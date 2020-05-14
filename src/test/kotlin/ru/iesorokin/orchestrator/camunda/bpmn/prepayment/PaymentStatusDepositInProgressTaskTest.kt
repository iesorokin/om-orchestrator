package ru.iesorokin.orchestrator.camunda.bpmn.prepayment

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Before
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement

class PaymentStatusDepositInProgressTaskTest : PrepaymentCamundaTest() {
    private val paymentTaskId = "12345678"

    @Before
    fun setUp() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .startBeforeActivity(BusinessProcessElement.PAYMENT_STATUS_DEPOSIT_IN_PROGRESS.code)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted
    }

    @Test
    fun `execute should update task status to deposit in progress with 3 retires`() {
        whenever(paymentTaskService.updateTaskStatus(paymentTaskId, PaymentTransactionStatus.DEPOSIT_IN_PROGRESS)).thenThrow(RuntimeException())

        assertJobRetry(retryCount = 3, retryMinutes = 5)
        BpmnAwareAssertions.assertThat(processInstance).hasNotPassed(BusinessProcessElement.PAYMENT_STATUS_DEPOSIT_IN_PROGRESS.code)
        verify(paymentTaskService, times(3)).updateTaskStatus(paymentTaskId, PaymentTransactionStatus.DEPOSIT_IN_PROGRESS)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }

    @Test
    fun `execute should update workflowId in payment task with one retry`() {
        whenever(paymentTaskService.updateTaskStatus(paymentTaskId, PaymentTransactionStatus.DEPOSIT_IN_PROGRESS))
                .thenThrow(RuntimeException())
                .then {}

        val retries = executeJob(processInstance.processInstanceId)
        Assertions.assertThat(retries).isEqualTo(2)
        assertLockExpirationTime(5)
        executeJob(processInstance.processInstanceId)

        BpmnAwareAssertions.assertThat(processInstance).hasPassed(BusinessProcessElement.PAYMENT_STATUS_DEPOSIT_IN_PROGRESS.code)
        verify(paymentTaskService, times(2)).updateTaskStatus(paymentTaskId, PaymentTransactionStatus.DEPOSIT_IN_PROGRESS)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }

}
