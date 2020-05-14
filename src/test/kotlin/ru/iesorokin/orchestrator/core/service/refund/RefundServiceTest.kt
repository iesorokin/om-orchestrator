package ru.iesorokin.orchestrator.core.service.refund

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.mockito.Mockito.never
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.domain.RefundLine
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.output.stream.sender.ConductorSender
import ru.iesorokin.orchestrator.output.stream.sender.dto.conductor.RefundMessage
import ru.iesorokin.orchestrator.output.stream.sender.dto.conductor.RefundMessageLine
import ru.iesorokin.orchestrator.output.stream.sender.dto.conductor.RefundType
import java.math.BigDecimal

class RefundServiceTest {
    private val paymentTaskService = mock<PaymentTaskService>()
    private val refundContextService = spy<RefundContextService>()
    private val conductorSender = mock<ConductorSender>()
    private val refundService = RefundService(
            paymentTaskService = paymentTaskService,
            refundContextService = refundContextService,
            conductorSender = conductorSender
    )

    private val taskId = "taskId"

    @Test
    fun `should send refund message`() {
        //Given
        val paymentTask = paymentTask()
        val refundMessage = buildRefundMessage()
        whenever(paymentTaskService.getPaymentTask(taskId)).thenReturn(paymentTask)
        whenever(refundContextService.buildRefundLines(paymentTask)).thenReturn(buildRefundLines())

        //When
        refundService.startRefundProcess(taskId)

        //Then
        verify(conductorSender, times(1)).sendRefundMessage(refundMessage)
    }

    @Test
    fun `should not start refund process if refund lines is empty`() {
        //Given
        val paymentTask = paymentTask()
        whenever(paymentTaskService.getPaymentTask(taskId)).thenReturn(paymentTask)
        whenever(refundContextService.buildRefundLines(paymentTask)).thenReturn(emptyList())

        //When
        refundService.startRefundProcess(taskId)

        //Then
        verify(conductorSender, never()).sendRefundMessage(any())
    }

    private fun paymentTask(): PaymentTask =
            PaymentTask(
                    taskId = taskId,
                    taskStatus = "PAID",
                    extOrderId = "extOrderId",
                    taskType = "SOLUTION",
                    lines = listOf()
            )

    private fun buildRefundMessage(): RefundMessage =
            RefundMessage(
                    refundType = RefundType.PARTIAL,
                    extOrderId = "extOrderId",
                    paymentTaskId = taskId,
                    currentPaymentStatus = "PAID",
                    taskType = "SOLUTION",
                    lines = listOf(
                            RefundMessageLine(
                                    extLineId = "extLineId",
                                    quantity = BigDecimal.ONE,
                                    unitAmountIncludingVat = BigDecimal.TEN
                            )
                    )
            )

    private fun buildRefundLines(): List<RefundLine> =
            listOf(
                    RefundLine(
                            extLineId = "extLineId",
                            quantity = BigDecimal.ONE,
                            unitAmountIncludingVat = BigDecimal.TEN
                    )
            )
}
