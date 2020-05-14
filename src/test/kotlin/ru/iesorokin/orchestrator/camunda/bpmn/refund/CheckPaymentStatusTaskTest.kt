package ru.iesorokin.payment.orchestrator.camunda.bpmn.refund

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.iesorokin.payment.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.payment.getFileAsObject
import ru.iesorokin.payment.orchestrator.camunda.bpmn.refund.base.RefundCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.HOLD
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus.PAID
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.RefundProcessElement.CHECK_CURRENT_PAYMENT_STATUS_TASK
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.RefundProcessElement.REFUND_TP_NET_TASK

class CheckPaymentStatusTaskTest : RefundCamundaTest() {

    private val paymentTaskId = "paymentTaskIdForCheckPaymentStatus"
    private lateinit var task: PaymentTask

    @Before
    fun setUp() {
        task = getFileAsObject<PaymentTask>("${PAYMENT_TASK_FILE_PATH}task-success.json")
    }

    private fun startProcess() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .startBeforeActivity(CHECK_CURRENT_PAYMENT_STATUS_TASK.code)
                .businessKey("businessKey")
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .execute()
        assertThat(processInstance).isStarted
    }

    @Test
    fun `should success passed`() {
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(task)

        startProcess()

        executeJob(processInstance.processInstanceId)
        assertThat(processInstance).hasPassed(CHECK_CURRENT_PAYMENT_STATUS_TASK.code)
        assertThat(processInstance).isWaitingAt(REFUND_TP_NET_TASK.code)
        verify(paymentTaskService, times(1)).getPaymentTask(paymentTaskId)
        verify(validationService, times(1)).checkPaymentStatusForRefund(paymentTaskId, PAID)
    }

    @Test
    fun `should retry task check 48 times with 30 minutes interval if checking is failed`() {
        // Given
        whenever(paymentTaskService.getPaymentTask(paymentTaskId)).thenReturn(task.copy(taskStatus = "HOLD"))

        // When
        startProcess()
        assertJobRetry(retryCount = 48, retryMinutes = 30)

        // Then
        assertThat(processInstance).isWaitingAt(CHECK_CURRENT_PAYMENT_STATUS_TASK.code)
        verify(paymentTaskService, times(48)).getPaymentTask(paymentTaskId)
        verify(validationService, times(48)).checkPaymentStatusForRefund(paymentTaskId, HOLD)
    }

    @After
    fun tearDown() {
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }
}
