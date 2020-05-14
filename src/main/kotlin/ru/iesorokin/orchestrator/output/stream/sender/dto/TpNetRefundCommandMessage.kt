package ru.iesorokin.payment.orchestrator.output.stream.sender.dto


data class TpNetRefundCommandMessage(val paymentTaskId: String,
                                     val refundWorkflowId: String)
