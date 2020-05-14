package ru.iesorokin.payment.orchestrator.output.client.payment.task.dto

import java.math.BigDecimal

data class PatchRefundRequest(
        val status: String,
        val refundWorkflowId: String,
        val lines: Collection<PatchRefundLine>
)

data class PatchRefundLine(
        val extLineId: String,
        val quantity: BigDecimal,
        val unitAmountIncludingVat: BigDecimal
)

enum class RefundStatus {
    REFUND,
    UNHOLD,
    REFUND_AFTER_GIVEAWAY
}