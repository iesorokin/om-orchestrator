package ru.iesorokin.payment.orchestrator.input.stream.receiver.dto

data class TpNetDepositEventMessage(val paymentTaskId: String)
data class TpNetRefundEventMessage(val paymentTaskId: String)
data class TpNetGiveAwayEventMessage(val paymentTaskId: String)
