package ru.iesorokin.payment.orchestrator.core.service.refund

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.iesorokin.payment.getFileAsObject
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.RefundLine

internal class RefundContextServiceTest {

    private val refundContextService = RefundContextService()

    @Test
    fun `buildRefundLines should works correctly`() {
        // Given
        val paymentTask = getFileAsObject<PaymentTask>("paymentTask/task-after-atol-registering.json")
        val expected = listOf(
                RefundLine(
                        extLineId = "4f9c29c2-62cb-4910-93b9-34db26f389f5",
                        quantity = "2".toBigDecimal(),
                        unitAmountIncludingVat = "156.232221".toBigDecimal()
                )
        )

        // When
        val actual = refundContextService.buildRefundLines(paymentTask)

        // Then
        assertThat(actual).isEqualTo(expected)
    }
}