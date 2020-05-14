package ru.iesorokin.orchestrator.core.service.prepayment

import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.messaging.support.MessageBuilder
import ru.iesorokin.PAYMENT_TASK_FILE_PATH
import ru.iesorokin.getFileAsObject
import ru.iesorokin.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.orchestrator.config.MessagingSource
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.FULL_APPROVE_KEY
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.APPROVE_PAYMENT_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.RECEIVE_PAYEMNT_TASK_COMPLETION
import ru.iesorokin.orchestrator.core.service.PaymentTaskService

class PaymentTaskCommandServiceTest : BaseSpringBootWithCamundaTest() {

    @Autowired
    private lateinit var messagingSource: MessagingSource
    @MockBean
    private lateinit var paymentTaskService: PaymentTaskService

    private lateinit var processInstance: ProcessInstance
    private val paymentTaskId = "12345"
    private val extOrderId = "extOrderId"

    @Test
    fun `executeCommand completes task and when FULL_APPROVE_KEY does not exist | response - fullApprove = true | result - true (for businessKey)`() {
        checkMessageApprove(
                variables = mapOf(
                        EXT_ORDER_ID to extOrderId
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200.json",
                expected = true,
                businessKey = paymentTaskId
        )
    }

    @Test
    fun `executeCommand APPROVE in context - not exist | response - fullApprove = false | result - false (for businessKey)`() {
        checkMessageApprove(
                variables = mapOf(
                        EXT_ORDER_ID to extOrderId
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200-2.json",
                expected = false,
                businessKey = paymentTaskId
        )
    }

    @Test
    fun `executeCommand APPROVE in context - false | response - fullApprove = true | result - false (for businessKey)`() {
        checkMessageApprove(
                variables = mapOf(
                        EXT_ORDER_ID to extOrderId,
                        FULL_APPROVE_KEY to false
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200.json",
                expected = false,
                businessKey = paymentTaskId
        )
    }

    @Test
    fun `executeCommand APPROVE in context - false | response - fullApprove = false | result - false (for businessKey)`() {
        checkMessageApprove(
                variables = mapOf(
                        EXT_ORDER_ID to extOrderId,
                        FULL_APPROVE_KEY to false
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200-2.json",
                expected = false,
                businessKey = paymentTaskId
        )
    }

    @Test
    fun `executeCommand APPROVE in context - true | response - fullApprove = true | result - true (for businessKey)`() {
        checkMessageApprove(
                variables = mapOf(
                        EXT_ORDER_ID to extOrderId,
                        FULL_APPROVE_KEY to true
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200.json",
                expected = true,
                businessKey = paymentTaskId
        )
    }

    @Test
    fun `executeCommand APPROVE in context - true | response - fullApprove = false | result - false (for businessKey)`() {
        checkMessageApprove(
                variables = mapOf(
                        EXT_ORDER_ID to extOrderId,
                        FULL_APPROVE_KEY to true
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200-2.json",
                expected = false,
                businessKey = paymentTaskId
        )
    }

    @Test
    fun `executeCommand COMPLETE should correlate receiveTaskCompletion message (for businessKey)`() {
        processInstance = startProcessBefore(
                activity = RECEIVE_PAYEMNT_TASK_COMPLETION,
                businessKey = paymentTaskId
        )
        @Language("json")
        val json = """
            {
              "paymentTaskId": "$paymentTaskId"
            }
        """.trimIndent()
        messagingSource.paymentOrchestratorCommandInput().send(
                MessageBuilder
                        .withPayload(json)
                        .setHeader("routeTo", "complete")
                        .build())

        BpmnAwareAssertions.assertThat(processInstance).hasPassed(RECEIVE_PAYEMNT_TASK_COMPLETION.code)
        endProcess(processInstance)
    }

    //@TODO remove all tests under this line when SBERBANK_PREPAYMENT_WITH_TPNET will start by businessKey and all processInstances without businessKey will be completed

    @Test
    fun `executeCommand APPROVE in context - not exist | response - fullApprove = true | result - true`() {
        checkMessageApprove(
                variables = mapOf(
                        PAYMENT_TASK_ID to paymentTaskId,
                        EXT_ORDER_ID to extOrderId
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200.json",
                expected = true
        )
    }

    @Test
    fun `executeCommand APPROVE in context - not exist | response - fullApprove = false | result - false`() {
        checkMessageApprove(
                variables = mapOf(
                        PAYMENT_TASK_ID to paymentTaskId,
                        EXT_ORDER_ID to extOrderId
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200-2.json",
                expected = false
        )
    }

    @Test
    fun `executeCommand APPROVE in context - false | response - fullApprove = true | result - false`() {
        checkMessageApprove(
                variables = mapOf(
                        PAYMENT_TASK_ID to paymentTaskId,
                        EXT_ORDER_ID to extOrderId,
                        FULL_APPROVE_KEY to false
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200.json",
                expected = false
        )
    }

    @Test
    fun `executeCommand APPROVE in context - false | response - fullApprove = false | result - false`() {
        checkMessageApprove(
                variables = mapOf(
                        PAYMENT_TASK_ID to paymentTaskId,
                        EXT_ORDER_ID to extOrderId,
                        FULL_APPROVE_KEY to false
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200-2.json",
                expected = false
        )
    }

    @Test
    fun `executeCommand APPROVE in context - true | response - fullApprove = true | result - true`() {
        checkMessageApprove(
                variables = mapOf(
                        PAYMENT_TASK_ID to paymentTaskId,
                        EXT_ORDER_ID to extOrderId,
                        FULL_APPROVE_KEY to true
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200.json",
                expected = true
        )
    }

    @Test
    fun `executeCommand APPROVE in context - true | response - fullApprove = false | result - false`() {
        checkMessageApprove(
                variables = mapOf(
                        PAYMENT_TASK_ID to paymentTaskId,
                        EXT_ORDER_ID to extOrderId,
                        FULL_APPROVE_KEY to true
                ),
                path = "${PAYMENT_TASK_FILE_PATH}task-response-200-2.json",
                expected = false
        )
    }

    @Test
    fun `executeCommand COMPLETE should correlate receiveTaskCompletion message`() {
        processInstance = startProcessBefore(
                activity = RECEIVE_PAYEMNT_TASK_COMPLETION,
                variables = mapOf(PAYMENT_TASK_ID to paymentTaskId)
        )
        @Language("json")
        val json = """
            {
              "paymentTaskId": "$paymentTaskId"
            }
        """.trimIndent()
        messagingSource.paymentOrchestratorCommandInput().send(
                MessageBuilder
                        .withPayload(json)
                        .setHeader("routeTo", "complete")
                        .build())

        BpmnAwareAssertions.assertThat(processInstance).hasPassed(RECEIVE_PAYEMNT_TASK_COMPLETION.code)
        endProcess(processInstance)
    }

    private fun checkMessageApprove(variables: Map<String,Any>, path: String, expected: Boolean, businessKey: String? = null) {
        processInstance = startProcessBefore(APPROVE_PAYMENT_TASK, variables, businessKey)
        whenever(paymentTaskService.getPaymentTask(paymentTaskId))
                .thenReturn(getFileAsObject(path))

        @Language("json")
        val json = """
            {
              "paymentTaskId": "$paymentTaskId"
            }
        """.trimIndent()

        messagingSource.approvePaymentTaskInput().send(MessageBuilder.withPayload(json).build())

        BpmnAwareAssertions.assertThat(processInstance).hasPassed(APPROVE_PAYMENT_TASK.code)

        Assertions.assertThat(expected)
                .isEqualTo(rule.runtimeService
                        .getVariable(processInstance.processInstanceId, FULL_APPROVE_KEY))
        endProcess(processInstance)
    }

    private fun startProcessBefore(activity: BusinessProcessElement,
                                   variables: Map<String,Any>? = null, businessKey: String? = null): ProcessInstance {
        val processInstanceBuilder = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_PREPAYMENT_WITH_TPNET.processName)
                .startBeforeActivity(activity.code)
                .businessKey(businessKey)
                .setVariables(variables)

        val processInstance = processInstanceBuilder.execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted

        BpmnAwareAssertions.assertThat(processInstance)
                .isWaitingAt(activity.code)

        return processInstance
    }
}
