package ru.iesorokin.payment.orchestrator.camunda.bpmn.tpNetInteraction

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.camunda.bpmn.tpNetInteraction.base.TpNetInteractionCamundaTest
import ru.iesorokin.payment.orchestrator.core.constants.process.tpnetinteraction.TP_NET_TYPE
import ru.iesorokin.payment.orchestrator.core.constants.process.tpnetinteraction.TP_NET_TYPE_DEPOSIT
import ru.iesorokin.payment.orchestrator.core.constants.process.tpnetinteraction.TP_NET_TYPE_GIVEAWAY
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.PLACE_TP_NET_DEPOSIT
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.PLACE_TP_NET_GIVEAWAY
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.TpNetInteractionProcessElement.RESOLVE_SEQUENCE

class ResolveSequenceTest : TpNetInteractionCamundaTest() {

    @Test
    fun `resolveSequence passed by deposit flow`() {
        // Given
        startTpNetInteractionProcess(
                RESOLVE_SEQUENCE,
                mapOf(TP_NET_TYPE to TP_NET_TYPE_DEPOSIT)
        )

        // When
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).hasPassed(RESOLVE_SEQUENCE.code)
        assertThat(processInstance).isWaitingAt(PLACE_TP_NET_DEPOSIT.code)
        rule.endProcess(processInstance)
    }

    @Test
    fun `resolveSequence passed by giveAway flow`() {
        // Given
        startTpNetInteractionProcess(
                RESOLVE_SEQUENCE,
                mapOf(TP_NET_TYPE to TP_NET_TYPE_GIVEAWAY)
        )

        // When
        executeJob(processInstance.processInstanceId)

        // Then
        assertThat(processInstance).hasPassed(RESOLVE_SEQUENCE.code)
        assertThat(processInstance).isWaitingAt(PLACE_TP_NET_GIVEAWAY.code)
        rule.endProcess(processInstance)
    }
}