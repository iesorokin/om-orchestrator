package ru.iesorokin.payment.orchestrator.camunda.bpmn.pod

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Before
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.camunda.bpmn.pod.base.PodCamundaTest
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.TPNET_DEPOSIT_FAIL
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.TPNET_DEPOSIT_SUCCESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process.POD_PAYMENT
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement.CHANGE_TASK_STATUS_TO_PAID
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement.RECEIVE_TP_NET_DEPOSIT_FAIL
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement.RECEIVE_TP_NET_DEPOSIT_SUCCESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement.TRY_PLACE_DEPOSIT
import kotlin.test.assertTrue

class ReceiveTpnetDepositTest : PodCamundaTest() {

    @Before
    fun preparation() {
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(POD_PAYMENT.processName)
                .startAfterActivity(PodProcessElement.PLACE_TP_NET_DEPOSIT.code)
                .execute()
        assertThat(processInstance).isStarted

        val activeSubscriptions = getActiveSubscriptions(processInstance.processInstanceId)
        assertTrue(activeSubscriptions.toString()) {
            activeSubscriptions.containsAll(listOf(
                    TPNET_DEPOSIT_SUCCESS.message, TPNET_DEPOSIT_FAIL.message
            ))
        }
    }

    @Test
    fun `receiveTpnetDepositSuccess correlated correctly`() {
        // When
        rule.runtimeService
                .createMessageCorrelation(TPNET_DEPOSIT_SUCCESS.message)
                .processInstanceId(processInstance.processInstanceId)
                .correlateWithResult()
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).isStarted
        assertThat(processInstance).hasPassed(RECEIVE_TP_NET_DEPOSIT_SUCCESS.code)
        assertThat(processInstance).isWaitingAt(CHANGE_TASK_STATUS_TO_PAID.code)
        rule.endProcess(processInstance)
    }

    @Test
    fun `receiveTpNetDepositFail correlated correctly`() {
        // When
        rule.runtimeService
                .createMessageCorrelation(TPNET_DEPOSIT_FAIL.message)
                .processInstanceId(processInstance.processInstanceId)
                .correlateWithResult()
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).isStarted
        assertThat(processInstance).hasPassed(RECEIVE_TP_NET_DEPOSIT_FAIL.code)
        assertThat(processInstance).isWaitingAt(TRY_PLACE_DEPOSIT.code)
        rule.endProcess(processInstance)
    }

}