package ru.iesorokin.payment.orchestrator.output.stream.sender.dto

import java.math.BigDecimal

data class SberbankDepositCommandMessage(
        val orderId: String,
        val depositAmount: BigDecimal,
        val storeId: Int,
        val correlationKey: String? = null
)
