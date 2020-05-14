package ru.iesorokin.payment.orchestrator.core.domain

data class TpnetItsmTicket(
        val paymentTaskId: String,
        val processInstanceId: String,
        val tpnetOperationType: String
)