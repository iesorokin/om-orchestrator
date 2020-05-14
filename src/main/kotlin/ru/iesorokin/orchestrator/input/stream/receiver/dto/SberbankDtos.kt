package ru.iesorokin.payment.orchestrator.input.stream.receiver.dto

data class SberbankDepositEventMessage(val orderId: String,
                                       val message: String? = null,
                                       val correlationKey: String? = null)
