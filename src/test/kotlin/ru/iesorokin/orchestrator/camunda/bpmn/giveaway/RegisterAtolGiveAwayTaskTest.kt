package ru.iesorokin.payment.orchestrator.camunda.bpmn.giveaway

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_GIVE_AWAY_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.EXECUTION_STORE
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.BusinessProcessElement.REGISTER_ATOL_GIVE_AWAY_TASK

class RegisterAtolGiveAwayTaskTest : GiveAwayCamundaTest() {

    @Test
    fun `should save atol give away id without giveAwayId in context`() {
        val paymentTaskId = "paymentTaskId"
        val solutionId = "solutionId"
        val storeId = 49
        val atolGiveAwayId = "atolGiveAwayId"

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(Process.PAYMENT_GIVEAWAY.processName)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariable(EXT_ORDER_ID, solutionId)
                .setVariable(EXECUTION_STORE, storeId)
                .execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted

        whenever(atolService.registerAtolGiveAway(
                solutionId = eq(solutionId),
                paymentTaskId = eq(paymentTaskId),
                processInstanceId = any(),
                giveAwayId = eq(null),
                correlationKey = eq(null),
                storeId = eq(storeId)
        )).thenReturn(atolGiveAwayId)

        executeJob(processInstanceId = processInstance.processInstanceId, activityId = REGISTER_ATOL_GIVE_AWAY_TASK.code)
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(REGISTER_ATOL_GIVE_AWAY_TASK.code)

        val variable = rule.runtimeService.getVariable(processInstance.processInstanceId, ATOL_GIVE_AWAY_ID)
        assertThat(variable).isEqualTo(atolGiveAwayId)

        rule.endProcess(processInstance)
    }

    @Test
    fun `should save atol give away id when giveAwayId exists in context`() {
        val paymentTaskId = "paymentTaskId"
        val solutionId = "solutionId"
        val storeId = 49
        val atolGiveAwayId = "atolGiveAwayId"
        val giveAwayId = "giveAwayId"

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(Process.PAYMENT_GIVEAWAY.processName)
                .businessKey(giveAwayId)
                .setVariable(PAYMENT_TASK_ID, paymentTaskId)
                .setVariable(EXT_ORDER_ID, solutionId)
                .setVariable(EXECUTION_STORE, storeId)
                .execute()
        BpmnAwareAssertions.assertThat(processInstance).isStarted

        whenever(atolService.registerAtolGiveAway(
                solutionId = eq(solutionId),
                paymentTaskId = eq(paymentTaskId),
                processInstanceId = eq(processInstance.processInstanceId),
                giveAwayId = eq(giveAwayId),
                correlationKey = eq(giveAwayId),
                storeId = eq(storeId)
        )).thenReturn(atolGiveAwayId)

        executeJob(processInstanceId = processInstance.processInstanceId, activityId = REGISTER_ATOL_GIVE_AWAY_TASK.code)
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(REGISTER_ATOL_GIVE_AWAY_TASK.code)

        val variable = rule.runtimeService.getVariable(processInstance.processInstanceId, ATOL_GIVE_AWAY_ID)
        assertThat(variable).isEqualTo(atolGiveAwayId)

        rule.endProcess(processInstance)
    }
}