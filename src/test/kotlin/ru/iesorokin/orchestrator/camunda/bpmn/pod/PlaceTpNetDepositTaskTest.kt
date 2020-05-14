package ru.iesorokin.orchestrator.camunda.bpmn.pod

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Test
import ru.iesorokin.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.orchestrator.camunda.bpmn.pod.base.PodCamundaTest
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.PodProcessElement.PLACE_TP_NET_DEPOSIT

class PlaceTpNetDepositTaskTest : PodCamundaTest() {

    @Test
    fun `placeTpNetDepositTask passed correctly`() {
        // When
        startPodProcess(PLACE_TP_NET_DEPOSIT)
        executeJob(processInstance.processInstanceId)

        // Then
        verify(tpNetService, times(1)).doDeposit(testPaymentTaskId)
        assertThat(processInstance).hasPassed(PLACE_TP_NET_DEPOSIT.code)
        rule.endProcess(processInstance)
    }

}