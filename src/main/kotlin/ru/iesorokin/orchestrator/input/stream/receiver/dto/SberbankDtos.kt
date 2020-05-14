package ru.iesorokin.orchestrator.input.stream.receiver.dto

data class SberbankDepositEventMessage(val orderId: String,
                                       val message: String? = null,
                                       val correlationKey: String? = null)
