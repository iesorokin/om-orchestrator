package ru.iesorokin.payment.orchestrator.output.client.dto

import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus

data class UpdateTaskStatusRequest(
        val updatedBy: String,
        val status: PaymentTransactionStatus
)
