package ru.iesorokin.orchestrator.core.service.prepayment

import org.camunda.bpm.engine.test.assertions.ProcessEngineTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.beans.factory.annotation.Autowired
import ru.iesorokin.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.CANCEL_PREPAYMENT_TASK
import ru.iesorokin.orchestrator.input.stream.receiver.dto.CancelProcessMessage

class ProcessCancellationServiceTest : BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var processCancellationService: ProcessCancellationService

    @get:Rule
    val thrown = ExpectedException.none()!!
    private val paymentTaskId = "paymentTaskIdCancellationService"

    @Test
    fun `should successfully processed cancelMessage when businessKey is used`() {
        val cancellationMessage = CancelProcessMessage(
                paymentTaskId = paymentTaskId, currentPaymentStatus = PaymentTransactionStatus.CONFIRMED.name)

        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET.processName)
                .businessKey(paymentTaskId)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted

        processCancellationService.cancelPrepaymentProcess(cancellationMessage)
        ProcessEngineTests.assertThat(processInstance).isWaitingAt(CANCEL_PREPAYMENT_TASK.code)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }

    @Test
    fun `should not processed cancelMessage if payment task status is COMPLETED when businessKey is used`() {
        val cancellationMessage = CancelProcessMessage(
                paymentTaskId = paymentTaskId, currentPaymentStatus = PaymentTransactionStatus.COMPLETED.name)

        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET.processName)
                .businessKey(paymentTaskId)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted

        processCancellationService.cancelPrepaymentProcess(cancellationMessage)
        ProcessEngineTests.assertThat(processInstance).isNotWaitingAt(CANCEL_PREPAYMENT_TASK.code)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }

    @Test
    //@TODO remove all tests after this line when SBERBANK_PREPAYMENT_WITH_TPNET will start by businessKey and all processInstances without businessKey will be completed
    fun `should successfully processed cancelMessage`() {
        val cancellationMessage = CancelProcessMessage(
                paymentTaskId = paymentTaskId, currentPaymentStatus = PaymentTransactionStatus.CONFIRMED.name)
        val context = mapOf(
                PAYMENT_TASK_ID to cancellationMessage.paymentTaskId as Any
        )

        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET.processName)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariables(context)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted

        processCancellationService.cancelPrepaymentProcess(cancellationMessage)
        ProcessEngineTests.assertThat(processInstance).isWaitingAt(CANCEL_PREPAYMENT_TASK.code)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }

    @Test
    fun `should not  processed cancelMessage if payment task status is COMPLETED`() {
        val cancellationMessage = CancelProcessMessage(
                paymentTaskId = paymentTaskId, currentPaymentStatus = PaymentTransactionStatus.COMPLETED.name)
        val context = mapOf(
                PAYMENT_TASK_ID to cancellationMessage.paymentTaskId as Any
        )

        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET.processName)
                .setVariables(context)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted

        processCancellationService.cancelPrepaymentProcess(cancellationMessage)
        ProcessEngineTests.assertThat(processInstance).isNotWaitingAt(CANCEL_PREPAYMENT_TASK.code)
        rule.runtimeService.deleteProcessInstance(processInstance.processInstanceId, "endOfTest")
    }
}
