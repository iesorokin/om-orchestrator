package ru.iesorokin.orchestrator.output.stream.sender.dto

data class TpnetItsmTicketMessage(
    val paymentTaskId: String,
    val processInstanceId: String,
    val tpnetOperationType: String
)
