package ru.iesorokin.payment.orchestrator.output.stream.sender.dto

data class FiscalizationStatusMessage(
    val paymentTaskId: String,
    val extOrderId: String,
    val status: String
)