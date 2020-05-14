package ru.iesorokin.payment.orchestrator.input.stream.receiver.dto

import java.math.BigDecimal

data class RefundPaymentMessage(val extOrderId: String,
                                val paymentTaskId: String,
                                val currentPaymentStatus: String,
                                val lines: Collection<RefundLineMessage>)

data class RefundLineMessage(val extLineId: String,
                             val quantity: BigDecimal,
                             val unitAmountIncludingVat: BigDecimal)
