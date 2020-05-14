package ru.iesorokin.orchestrator.output.client.dto

import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus

data class UpdateTaskStatusRequest(
        val updatedBy: String,
        val status: PaymentTransactionStatus
)
