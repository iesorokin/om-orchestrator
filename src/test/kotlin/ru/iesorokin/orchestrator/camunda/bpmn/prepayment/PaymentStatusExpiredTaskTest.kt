package ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Before
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.prepayment.base.PrepaymentCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BoundaryEventType
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement

class PaymentStatusExpiredTaskTest : PrepaymentCamundaTest() {

    private val paymentTaskId = "12345678"

    @Before
    fun setUp() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .startAfterActivity(BoundaryEventType.EXPIRED_PAYMENT_TASK_MESSAGE.eventName)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted
    }

    @Test
    fun `execute should update payment task status with 3 retires`() {
        whenever(paymentTaskService.updateTaskStatus(paymentTaskId, PaymentTransactionStatus.EXPIRED))
                .thenThrow(RuntimeException())

        assertJobRetry(retryCount = 3, retryMinutes = 5)
        BpmnAwareAssertions.assertThat(processInstance).hasNotPassed(BusinessProcessElement.PAYMENT_TASK_EXPIRED.code)
        verify(paymentTaskService, times(3)).updateTaskStatus(paymentTaskId, PaymentTransactionStatus.EXPIRED)

        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "test end")
    }

    @Test
    fun `execute should update payment task status with one retry`() {
        whenever(paymentTaskService.updateTaskStatus(paymentTaskId, PaymentTransactionStatus.EXPIRED))
                .thenThrow(RuntimeException())
                .thenAnswer {}

        val retries = executeJob(processInstance.processInstanceId)
        assertThat(retries).isEqualTo(2)
        assertLockExpirationTime(5)
        executeJob(processInstance.processInstanceId)

        BpmnAwareAssertions.assertThat(processInstance).hasPassed(BusinessProcessElement.PAYMENT_TASK_EXPIRED.code)
    }
}
