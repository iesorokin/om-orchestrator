package ru.iesorokin.payment.orchestrator.core.service.giveaway

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.junit.Test
import ru.iesorokin.payment.orchestrator.core.domain.GiveAway
import ru.iesorokin.payment.orchestrator.core.domain.GiveAwayExternalLine
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.TPNET_GIVE_AWAY_FAIL
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.TPNET_GIVE_AWAY_SUCCESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process.PAYMENT_GIVEAWAY
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process.TP_NET_INTERACTION
import ru.iesorokin.payment.orchestrator.core.exception.GiveAwaysNotFoundException
import ru.iesorokin.payment.orchestrator.core.service.CamundaService
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import java.math.BigDecimal
import java.time.LocalDateTime

class GiveAwayServiceTest {
    private val paymentTaskService = mock<PaymentTaskService>()
    private val camundaService = mock<CamundaService>()
    private val giveAwayService = GiveAwayService(paymentTaskService, camundaService)

    @Test
    fun `processSuccessGiveAway - sent TPNET_GIVE_AWAY_SUCCESS event to engine`() {
        //Given
        val paymentTaskId = "paymentTaskId"
        val processInstanceId = "processInstanceId"
        val processInstance = mock<ProcessInstance>()
        whenever(paymentTaskService.getGiveAways(paymentTaskId)).thenReturn(listOf(giveAway()))
        whenever(camundaService.getActiveProcessInstanceById(PAYMENT_GIVEAWAY)).thenReturn(
                mapOf(processInstanceId to processInstance)
        )
        whenever(camundaService.getActiveProcessInstanceByBusinessKey(PAYMENT_GIVEAWAY)).thenReturn(
                emptyMap()
        )

        //When
        giveAwayService.processSuccessGiveAway(paymentTaskId)

        //Then
        verify(paymentTaskService, times(1)).getGiveAways(paymentTaskId)
        verify(camundaService, times(1)).getActiveProcessInstanceById(PAYMENT_GIVEAWAY)
        verify(camundaService, times(1)).sendBusinessProcessMessage(processInstanceId, TPNET_GIVE_AWAY_SUCCESS)
        verify(camundaService, times(0)).createMessageCorrelation(any(), any(), any())
    }

    @Test
    fun `processSuccessGiveAway - sent TPNET_GIVE_AWAY_SUCCESS event to to tpNetInteraction process`() {
        //Given
        val paymentTaskId = "paymentTaskId"
        val processInstance = mock<ProcessInstance>()
        val giveAway = GiveAway(
                createdBy = "createdBy",
                created = LocalDateTime.now(),
                giveAwayId = "giveAwayId",
                lines = listOf(giveAwayExternalLine())
        )
        whenever(paymentTaskService.getGiveAways(paymentTaskId)).thenReturn(listOf(giveAway))
        whenever(camundaService.getActiveProcessInstanceById(PAYMENT_GIVEAWAY)).thenReturn(
                emptyMap()
        )
        whenever(camundaService.getActiveProcessInstanceByBusinessKey(PAYMENT_GIVEAWAY)).thenReturn(
                emptyMap()
        )
        whenever(camundaService.getActiveProcessInstanceByBusinessKey(TP_NET_INTERACTION)).thenReturn(
                mapOf(giveAway.giveAwayId!! to processInstance)
        )

        //When
        giveAwayService.processSuccessGiveAway(paymentTaskId)

        //Then
        verify(paymentTaskService, times(1)).getGiveAways(paymentTaskId)
        verify(camundaService, times(1)).getActiveProcessInstanceByBusinessKey(PAYMENT_GIVEAWAY)
        verify(camundaService, times(1)).getActiveProcessInstanceByBusinessKey(TP_NET_INTERACTION)
        verify(camundaService, times(1)).createMessageCorrelation(
                giveAway.giveAwayId!!, TPNET_GIVE_AWAY_SUCCESS, null
        )
        verify(camundaService, times(0)).sendBusinessProcessMessage(any(), any())
    }

    @Test(expected = GiveAwaysNotFoundException::class)
    fun `processSuccessGiveAway - not_ok - not found give away`() {
        //Given
        val paymentTaskId = "paymentTaskId"
        val processInstanceId = "notExistsProcessIds"
        val processInstance = mock<ProcessInstance>()
        whenever(paymentTaskService.getGiveAways(paymentTaskId)).thenReturn(listOf(giveAway()))
        whenever(camundaService.getActiveProcessInstanceById(PAYMENT_GIVEAWAY)).thenReturn(
                mapOf(processInstanceId to processInstance)
        )
        whenever(camundaService.getActiveProcessInstanceById(PAYMENT_GIVEAWAY)).thenReturn(
                emptyMap()
        )

        //When
        giveAwayService.processSuccessGiveAway(paymentTaskId)

        //Then
        verify(paymentTaskService, times(1)).getGiveAways(paymentTaskId)
        verify(camundaService, times(1)).getActiveProcessInstanceById(PAYMENT_GIVEAWAY)
        verify(camundaService, times(0)).sendBusinessProcessMessage(any(), any())
        verify(camundaService, times(0)).createMessageCorrelation(any(), any())
    }

    @Test
    fun `processFailedGiveAway - correlated TPNET_GIVE_AWAY_FAIL`() {
        //Given
        val paymentTaskId = "paymentTaskId"
        val businessKey = "giveAwayId"
        val processInstance = mock<ProcessInstance>()
        whenever(paymentTaskService.getGiveAways(paymentTaskId)).thenReturn(listOf(giveAway().copy(
                giveAwayId = "giveAwayId"
        )))
        whenever(camundaService.getActiveProcessInstanceById(PAYMENT_GIVEAWAY)).thenReturn(
                emptyMap()
        )
        whenever(camundaService.getActiveProcessInstanceByBusinessKey(PAYMENT_GIVEAWAY)).thenReturn(
                mapOf(businessKey to processInstance)
        )
        whenever(camundaService.getActiveProcessInstanceByBusinessKey(TP_NET_INTERACTION)).thenReturn(
                emptyMap()
        )

        //When
        giveAwayService.processFailedGiveAway(paymentTaskId)

        //Then
        verify(paymentTaskService, times(1)).getGiveAways(paymentTaskId)
        verify(camundaService, times(0)).sendBusinessProcessMessage(any(), any())
        verify(camundaService, times(1)).createMessageCorrelation(businessKey, TPNET_GIVE_AWAY_FAIL)
        verify(camundaService, times(0)).getActiveProcessInstanceById(PAYMENT_GIVEAWAY)
    }

    private fun giveAway(): GiveAway =
            GiveAway(
                    createdBy = "createdBy",
                    created = LocalDateTime.now(),
                    processInstanceId = "processInstanceId",
                    lines = listOf(giveAwayExternalLine())
            )

    private fun giveAwayExternalLine(): GiveAwayExternalLine =
            GiveAwayExternalLine(
                    extLineId = "extLineId",
                    itemCode = "itemCode",
                    lineId = "lineId",
                    unitAmountIncludingVat = BigDecimal.ONE,
                    quantity = BigDecimal.TEN
            )

}