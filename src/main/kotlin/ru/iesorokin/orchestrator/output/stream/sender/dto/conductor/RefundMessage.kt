package ru.iesorokin.payment.orchestrator.output.stream.sender.dto.conductor

import java.math.BigDecimal

data class RefundMessage(
        val refundType: RefundType,
        val extOrderId: String,
        val paymentTaskId: String,
        val currentPaymentStatus: String,
        val taskType: String,
        val lines: Collection<RefundMessageLine>
)

data class RefundMessageLine(
        val extLineId: String,
        val quantity: BigDecimal,
        val unitAmountIncludingVat: BigDecimal
)

enum class RefundType {
    PARTIAL
}