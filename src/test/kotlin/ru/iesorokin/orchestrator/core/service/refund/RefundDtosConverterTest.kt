package ru.iesorokin.payment.orchestrator.core.service.refund

import org.junit.Test
import ru.iesorokin.payment.orchestrator.core.domain.RefundContext
import ru.iesorokin.payment.orchestrator.core.domain.RefundLine
import ru.iesorokin.payment.orchestrator.output.client.dto.JsonPatchRequestOperation
import ru.iesorokin.payment.orchestrator.output.client.dto.OperationType
import ru.iesorokin.payment.orchestrator.output.client.payment.task.converter.PATCH_PATH_REFUND_STATUS_LIST
import ru.iesorokin.payment.orchestrator.output.client.payment.task.converter.PAYMENT_STATUS_APPROVE_IN_PROGRESS
import ru.iesorokin.payment.orchestrator.output.client.payment.task.converter.RefundDtosConverter
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.PatchRefundLine
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.PatchRefundRequest
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.RefundStatus
import java.math.BigDecimal
import kotlin.test.assertEquals

class RefundDtosConverterTest {

    private val refundDtosConverter = RefundDtosConverter()

    private val processInstanceId = "processInstanceId"

    @Test
    fun `should convert to request`() {
        //Given
        val expected = listOf(JsonPatchRequestOperation(
                OperationType.ADD,
                PATCH_PATH_REFUND_STATUS_LIST,
                patchRefundRequest()
        ))

        //When
        val actual = refundDtosConverter.toRequest(
                processInstanceId = processInstanceId,
                refundContext = refundContext()
        )

        //Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should convert to request with not approved in progress status`() {
        //Given
        val expected = listOf(JsonPatchRequestOperation(
                OperationType.ADD,
                PATCH_PATH_REFUND_STATUS_LIST,
                patchRefundRequest().copy(
                        status = RefundStatus.REFUND.name
                )
        ))

        //When
        val actual = refundDtosConverter.toRequest(
                processInstanceId = processInstanceId,
                refundContext = refundContext().copy(
                        currentPaymentStatus = "Not approved in progress status"
                )
        )

        //Then
        assertEquals(expected, actual)
    }

    @Test
    fun `should convert to request with completed status`() {
        //Given
        val expected = listOf(JsonPatchRequestOperation(
                OperationType.ADD,
                PATCH_PATH_REFUND_STATUS_LIST,
                patchRefundRequest().copy(
                        status = RefundStatus.REFUND_AFTER_GIVEAWAY.name
                )
        ))

        //When
        val actual = refundDtosConverter.toRequest(
                processInstanceId = processInstanceId,
                refundContext = refundContext().copy(
                        currentPaymentStatus = "COMPLETED"
                )
        )

        //Then
        assertEquals(expected, actual)
    }

    private fun refundContext(): RefundContext =
            RefundContext(
                    extOrderId = "extOrderId",
                    paymentTaskId = "paymentTaskId",
                    currentPaymentStatus = PAYMENT_STATUS_APPROVE_IN_PROGRESS,
                    lines = listOf(
                            RefundLine(
                                    extLineId = "extLineId",
                                    quantity = BigDecimal.TEN,
                                    unitAmountIncludingVat = BigDecimal.ONE
                            )
                    )
            )

    private fun patchRefundRequest(): PatchRefundRequest =
            PatchRefundRequest(
                    status = RefundStatus.UNHOLD.name,
                    refundWorkflowId = processInstanceId,
                    lines = listOf(
                            PatchRefundLine(
                                    extLineId = "extLineId",
                                    quantity = BigDecimal.TEN,
                                    unitAmountIncludingVat = BigDecimal.ONE
                            )
                    )
            )

}