package ru.iesorokin.payment.orchestrator.core.service

import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions
import org.junit.Test
import ru.iesorokin.payment.orchestrator.camunda.BaseSpringBootWithCamundaTest
import ru.iesorokin.payment.orchestrator.camunda.bpmn.endProcess
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REFUND_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REGISTER_ID
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskFiscalData
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.TPNET_REFUND_SUCCESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process.PAYMENT_GIVEAWAY
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.PodProcessElement
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.RefundProcessElement
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CamundaServiceTest : BaseSpringBootWithCamundaTest() {

    private lateinit var processInstance: ProcessInstance

    private val paymentTaskId = "paymentTaskId"
    private val atolRefundId = "atolRefundId"

    @Test
    fun `getRefundTpNetProcessByUniqueVariableForProcess | ok`() {
        val variableName = "variable"

        val context = mapOf(variableName to false)
        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .startBeforeActivity(RefundProcessElement.SOLVE_PROBLEM_SBERBANK.code)
                .setVariables(context)
                .execute()

        camundaService.updateVariableByProcessInstanceId(processInstance.processInstanceId, variableName, true)

        val actual = rule.runtimeService.getVariable(processInstance.processInstanceId, variableName)
        assertEquals(true, actual)

        rule.endProcess(processInstance)
    }

    @Test
    fun `updateVariableByProcessInstanceId should update variable`() {
        //Given
        val context = mapOf(ATOL_REFUND_ID to atolRefundId as Any)

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .startBeforeActivity(RefundProcessElement.SOLVE_PROBLEM_TPNET.code)
                .setVariables(context)
                .execute()

        //When
        val actual = camundaService.getRefundTpNetProcessByUniqueVariableForProcess(ATOL_REFUND_ID, atolRefundId)

        //Then
        assertEquals(processInstance.processInstanceId, actual.processInstanceId)

        rule.endProcess(processInstance)
    }

    @Test
    fun `startProcess should start PAYMENT_GIVEAWAY process with business key`() {
        val businessKey = UUID.randomUUID().toString()

        val processInstanceId = camundaService.startProcess(PAYMENT_GIVEAWAY, businessKey, emptyMap())

        val processInstance = rule.runtimeService.createProcessInstanceQuery()
                .active()
                .processInstanceBusinessKey(businessKey)
                .singleResult()
        assertEquals(processInstanceId, processInstance.id)

        rule.runtimeService.deleteProcessInstance(processInstanceId, "endOfTest")
    }

    @Test
    fun `startProcess should start PAYMENT_GIVEAWAY process`() {
        val processInstanceId = camundaService.startProcess(PAYMENT_GIVEAWAY, emptyMap())

        val processInstance = rule.runtimeService.createProcessInstanceQuery()
                .active()
                .processInstanceId(processInstanceId)
                .singleResult()
        assertEquals(processInstanceId, processInstance.id)

        rule.runtimeService.deleteProcessInstance(processInstanceId, "endOfTest")
    }

    @Test
    fun `startProcess should throw StartProcessException if attempt start process with same businessKey`() {
        val businessKey = UUID.randomUUID().toString()

        val processInstanceId = camundaService.startProcess(PAYMENT_GIVEAWAY, businessKey, emptyMap())

        val processInstance = rule.runtimeService.createProcessInstanceQuery()
                .active()
                .processInstanceBusinessKey(businessKey)
                .singleResult()
        assertEquals(processInstanceId, processInstance.id)

        rule.runtimeService.deleteProcessInstance(processInstanceId, "endOfTest")
    }

    @Test
    fun `getActiveProcessInstanceById - ok`() {
        //Given
        val context = mapOf("delayDuration" to "PT10M" as Any)

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(PAYMENT_GIVEAWAY.processName)
                .setVariables(context)
                .execute()

        val processInstance2 = rule.runtimeService
                .createProcessInstanceByKey(PAYMENT_GIVEAWAY.processName)
                .setVariables(context)
                .execute()

        //When
        val actual = camundaService.getActiveProcessInstanceById(PAYMENT_GIVEAWAY)

        //Then
        assertThat(actual).hasSize(2)
        assertThat(actual[processInstance.processInstanceId]?.processDefinitionId)
                .isEqualTo(processInstance.processDefinitionId)
        assertThat(actual[processInstance2.processInstanceId]?.processDefinitionId)
                .isEqualTo(processInstance2.processDefinitionId)

        rule.endProcess(processInstance)
        rule.endProcess(processInstance2)
    }

    @Test
    fun `getActiveProcessInstances finds all active instance`() {
        //Given
        val context = mapOf("delayDuration" to "PT10M" as Any)

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(PAYMENT_GIVEAWAY.processName)
                .setVariables(context)
                .execute()

        val processInstance2 = rule.runtimeService
                .createProcessInstanceByKey(PAYMENT_GIVEAWAY.processName)
                .setVariables(context)
                .execute()

        //When
        val actual = camundaService.getActiveProcessInstances(PAYMENT_GIVEAWAY)

        //Then
        assertThat(actual).hasSize(2)
        assertNotNull(actual.single { it.processInstanceId == processInstance.processInstanceId })
        assertNotNull(actual.single { it.processInstanceId == processInstance2.processInstanceId })

        rule.endProcess(processInstance)
        rule.endProcess(processInstance2)
    }

    @Test
    fun `getActiveProcessInstanceByBusinessKey - found all active process by business key`() {
        //Given
        val context = mapOf("delayDuration" to "PT10M" as Any)

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(PAYMENT_GIVEAWAY.processName)
                .businessKey("businessKey1")
                .setVariables(context)
                .execute()

        val processInstance2 = rule.runtimeService
                .createProcessInstanceByKey(PAYMENT_GIVEAWAY.processName)
                .businessKey("businessKey2")
                .setVariables(context)
                .execute()

        //When
        val actual = camundaService.getActiveProcessInstanceByBusinessKey(PAYMENT_GIVEAWAY)

        //Then
        assertThat(actual).hasSize(2)
        assertThat(actual[processInstance.businessKey]?.processDefinitionId)
                .isEqualTo(processInstance.processDefinitionId)
        assertThat(actual[processInstance2.businessKey]?.processDefinitionId)
                .isEqualTo(processInstance2.processDefinitionId)

        rule.endProcess(processInstance)
        rule.endProcess(processInstance2)
    }

    @Test
    fun `createMessageCorrelation - correlate and passed waiting receive task`() {
        // Given
        val context = mapOf("delayDuration" to "PT10M" as Any)
        val variableKey = "variableKey"
        val variablesToCorrelate = mapOf(variableKey to "variableValue")

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .businessKey("123")
                .startBeforeActivity(RefundProcessElement.RECEIVE_TP_NET_REFUND_SUCCESS_TASK.code)
                .setVariables(context)
                .execute()

        // When
        camundaService.createMessageCorrelation(processInstance.businessKey, TPNET_REFUND_SUCCESS, variablesToCorrelate)

        // Then
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(RefundProcessElement.RECEIVE_TP_NET_REFUND_SUCCESS_TASK.code)
        assertEquals(rule.runtimeService.getVariable(processInstance.processInstanceId, variableKey), variablesToCorrelate[variableKey])

        rule.endProcess(processInstance)
    }

    @Test
    fun `createMessageCorrelation with nullable variables correlate and passed correctly`() {
        // Given
        val variableKey = "delayDuration"
        val context = mapOf(variableKey to "PT10M" as Any)

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(SBERBANK_REFUND_WITH_TPNET.processName)
                .businessKey("123")
                .startBeforeActivity(RefundProcessElement.RECEIVE_TP_NET_REFUND_SUCCESS_TASK.code)
                .setVariables(context)
                .execute()

        // When
        camundaService.createMessageCorrelation(processInstance.businessKey, TPNET_REFUND_SUCCESS)

        // Then
        BpmnAwareAssertions.assertThat(processInstance).hasPassed(RefundProcessElement.RECEIVE_TP_NET_REFUND_SUCCESS_TASK.code)
        assertEquals(rule.runtimeService.getVariable(processInstance.processInstanceId, variableKey), context[variableKey])

        rule.endProcess(processInstance)
    }

    @Test
    fun `getPodPaymentProcessByVariable returns processInstance`() {
        val atolIdVal = "someAtolId"
        val context = mapOf(ATOL_REGISTER_ID to atolIdVal)

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(Process.POD_PAYMENT.processName)
                .startBeforeActivity(PodProcessElement.TRY_PLACE_DEPOSIT.code)
                .setVariables(context)
                .execute()

        val processInstance = camundaService.getPodPaymentProcessByVariable(ATOL_REGISTER_ID, atolIdVal)

        assertNotNull(processInstance)
        rule.endProcess(processInstance)
    }

    @Test(expected = KotlinNullPointerException::class)
    fun `getPodPaymentProcessByVariable throws exception when can not find process instance by variable`() {
        val atolIdVal = "someAtolId"
        val context = mapOf(ATOL_REGISTER_ID to atolIdVal)

        processInstance = rule.runtimeService
                .createProcessInstanceByKey(Process.POD_PAYMENT.processName)
                .startBeforeActivity(PodProcessElement.TRY_PLACE_DEPOSIT.code)
                .setVariables(context)
                .execute()

        camundaService.getPodPaymentProcessByVariable(ATOL_REGISTER_ID, "anotherValue")
    }

    @Test
    fun `getProcessInstanceByBusinessKeyAndProcess returns processInstance`() {
        val businessKey = "someBusinessKey"

        rule.runtimeService
                .createProcessInstanceByKey(Process.POD_PAYMENT.processName)
                .businessKey(businessKey)
                .startBeforeActivity(PodProcessElement.TRY_PLACE_DEPOSIT.code)
                .execute()

        val processInstance2 = rule.runtimeService
                .createProcessInstanceByKey(Process.POD_PAYMENT.processName)
                .businessKey("anotherBusinessKey")
                .startBeforeActivity(PodProcessElement.TRY_PLACE_DEPOSIT.code)
                .execute()

        val instance = camundaService.getProcessInstanceByBusinessKeyAndProcess(businessKey, Process.POD_PAYMENT)

        assertNotNull(instance)
        rule.endProcess(instance)
        rule.endProcess(processInstance2)
    }

    private fun paymentTask(): PaymentTask =
            PaymentTask(
                    taskId = paymentTaskId,
                    taskStatus = "REFUND",
                    taskType = "TP_NET_TASK_TYPE",
                    lines = listOf(),
                    refundStatusList = listOf()
            )

    private fun paymentTaskFiscalData(): PaymentTaskFiscalData =
            PaymentTaskFiscalData(
                    refundWorkflowId = "refundWorkflowId",
                    created = ZonedDateTime.now()
            )

}