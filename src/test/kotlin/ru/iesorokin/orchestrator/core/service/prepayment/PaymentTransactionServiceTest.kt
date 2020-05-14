package ru.iesorokin.payment.orchestrator.core.service.prepayment

import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BoundaryEventType
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.PaymentTransactionMessage
import kotlin.test.fail

class PaymentTransactionServiceTest : BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var paymentTransactionService: PaymentTransactionService

    private val paymentTaskId = "paymentTaskId"
    private val paymentTransaction = "paymentTransaction"

    @Test
    fun `processMessage should hold`() {
        val process = startProcess()
        paymentTransactionService.processMessage(
                PaymentTransactionMessage(
                        paymentTransaction = paymentTransaction,
                        status = PaymentTransactionStatus.HOLD
                )
        )

        BpmnAwareAssertions.assertThat(process)
                .hasPassed(BusinessProcessElement.RECEIVE_PAYMENT_TASK.code)

        BpmnAwareAssertions.assertThat(process)
                .hasNotPassed(BoundaryEventType.EXPIRED_PAYMENT_TASK_MESSAGE.eventName)
        endProcess(process)
    }


    @Test
    fun `processMessage should be expired`() {
        val process = startProcess()
        paymentTransactionService.processMessage(
                PaymentTransactionMessage(
                        paymentTransaction = paymentTransaction,
                        status = PaymentTransactionStatus.EXPIRED
                )
        )

        BpmnAwareAssertions.assertThat(process)
                .hasPassed(BusinessProcessElement.RECEIVE_PAYMENT_TASK.code)

        BpmnAwareAssertions.assertThat(process)
                .hasPassed(BoundaryEventType.EXPIRED_PAYMENT_TASK_MESSAGE.eventName)

        BpmnAwareAssertions.assertThat(process)
                .hasNotPassed(BusinessProcessElement.PAYMENT_TASK_EXPIRED.code)
        endProcess(process)
    }

    @Test(expected = ProcessEngineException::class)
    fun `should throw ProcessEngineException if process with variable not single`() {
        val process = startProcess()
        //start second process
        val secondProcess = startProcess()

        BpmnAwareAssertions.assertThat(secondProcess).isStarted
        try {
            paymentTransactionService.processMessage(
                    PaymentTransactionMessage(
                            paymentTransaction = paymentTransaction,
                            status = PaymentTransactionStatus.EXPIRED
                    )
            )

            fail("Should have thrown ProcessEngineException")
        } finally {
            BpmnAwareAssertions.assertThat(process)
                    .hasNotPassed(BusinessProcessElement.RECEIVE_PAYMENT_TASK.code)
            endProcess(process)
            endProcess(secondProcess)
        }
    }

    @Test(expected = KotlinNullPointerException::class)
    fun `should fail due to absence of processInstance`() {
        val process = startProcess()
        rule.runtimeService.deleteProcessInstance(process.processInstanceId, "no instances")
        BpmnAwareAssertions.assertThat(process).isEnded

        //we skip @After because processInstance is already deleted

        paymentTransactionService.processMessage(
                PaymentTransactionMessage(
                        paymentTransaction = paymentTransaction,
                        status = PaymentTransactionStatus.EXPIRED
                )
        )
        endProcess(process)
    }

    private fun startProcess(): ProcessInstance {
        val processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET)
                .startAfterActivity(BusinessProcessElement.SAVE_WORKFLOW_ID_TASK.code)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariable(EXT_ORDER_ID, paymentTransaction)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted

        return processInstance
    }
}
