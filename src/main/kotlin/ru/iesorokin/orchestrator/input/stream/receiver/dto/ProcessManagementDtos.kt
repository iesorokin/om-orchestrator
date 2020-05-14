package ru.iesorokin.payment.orchestrator.input.stream.receiver.dto

data class StartProcessMessage(val workflowType: String,
                               val extOrderId: String,
                               val paymentTaskId: String)

data class CancelProcessMessage(
        val paymentTaskId: String,
        val currentPaymentStatus: String
)
