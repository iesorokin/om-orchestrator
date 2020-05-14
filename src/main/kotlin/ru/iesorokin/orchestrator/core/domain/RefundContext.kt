package ru.iesorokin.payment.orchestrator.core.domain

import java.math.BigDecimal

data class RefundContext(
        val extOrderId: String,
        val paymentTaskId: String,
        val currentPaymentStatus: String,
        val lines: Collection<RefundLine>
)

data class RefundLine(
        val extLineId: String,
        val quantity: BigDecimal,
        val unitAmountIncludingVat: BigDecimal
)