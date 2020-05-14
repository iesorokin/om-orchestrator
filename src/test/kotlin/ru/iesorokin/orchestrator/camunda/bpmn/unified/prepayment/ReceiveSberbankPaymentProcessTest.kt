package ru.iesorokin.payment.orchestrator.camunda.bpmn.unified.prepayment

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Before
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process
import kotlin.test.assertTrue

class ReceiveSberbankPaymentProcessTest : UnifiedPrePaymentCamundaTest() {

    @Before
    fun setup() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(Process.UNIFIED_PREPAYMENT.processName)
                .startBeforeActivity(BusinessProcessElement.RECEIVE_PAYMENT_GATEWAY.code)
                .execute()

        BpmnAwareAssertions.assertThat(processInstance).isStarted

        val activeSubscriptions = getActiveSubscriptions(processInstance.processInstanceId)

        assertTrue("$(activeSubscriptions.toString())") {
            activeSubscriptions.containsAll(
                    listOf(
                            BusinessProcessEvent.PAYMENT_RECEIVED.message,
                            BusinessProcessEvent.PAYMENT_EXPIRED.message
                    )
            )
        }
    }

    @Test
    fun `should wait on receive payment task`() {
        BpmnAwareAssertions.assertThat(processInstance).isWaitingAt(BusinessProcessElement.RECEIVE_PAYMENT_GATEWAY.code).overridingErrorMessage("process instance supposed wait on ${BusinessProcessElement.RECEIVE_PAYMENT_GATEWAY.code}")

        rule.endProcess(processInstance)
    }
}