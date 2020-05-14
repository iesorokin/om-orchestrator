package ru.iesorokin.orchestrator.camunda.bpmn.refund

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import ru.iesorokin.ATOL_FILE_PATH
import ru.iesorokin.getFileAsObject
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.refund.base.RefundCamundaTest
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REFUND_ID
import ru.iesorokin.orchestrator.core.constants.process.CURRENT_PAYMENT_STATUS
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.RefundProcessElement.RECEIVE_ATOL_REFUND_SUCCESS_RECEIVE_TASK
import ru.iesorokin.orchestrator.input.stream.receiver.dto.AtolTransactionMessage
import kotlin.test.assertTrue

class AtolRefundSuccessMessageTest : RefundCamundaTest() {

    @Test
    fun `should successfully correlate received message and pass through the task`() {
        val atolMessage = getFileAsObject<AtolTransactionMessage>("${ATOL_FILE_PATH}atol-message-success.json")
        val context = mapOf(
                ATOL_REFUND_ID to atolMessage.atolId as Any,
                CURRENT_PAYMENT_STATUS to CURRENT_PAYMENT_STATUS
        )

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .startBeforeActivity(RECEIVE_ATOL_REFUND_SUCCESS_RECEIVE_TASK.code)
                .setVariables(context)
                .execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted

        BpmnAwareAssertions.assertThat(processInstance)
                .isWaitingAt(RECEIVE_ATOL_REFUND_SUCCESS_RECEIVE_TASK.code)

        val activeSubscriptions = getActiveSubscriptions(processInstance.processInstanceId)
        assertTrue(activeSubscriptions.toString()) {
            activeSubscriptions.containsAll(
                    listOf(
                            BusinessProcessEvent.ATOL_REFUND_SUCCESS.message
                    )
            )
        }

        rule.runtimeService.createMessageCorrelation(BusinessProcessEvent.ATOL_REFUND_SUCCESS.message)
                .processInstanceId(processInstance.processInstanceId)
                .correlateWithResult()

        BpmnAwareAssertions.assertThat(processInstance).hasPassed(RECEIVE_ATOL_REFUND_SUCCESS_RECEIVE_TASK.code)
        rule.endProcess(processInstance)
        BpmnAwareAssertions.assertThat(processInstance).isEnded
    }
}
