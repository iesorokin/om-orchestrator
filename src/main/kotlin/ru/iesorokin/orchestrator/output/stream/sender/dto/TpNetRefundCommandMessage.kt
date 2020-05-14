package ru.iesorokin.orchestrator.output.stream.sender.dto


data class TpNetRefundCommandMessage(val paymentTaskId: String,
                                     val refundWorkflowId: String)
