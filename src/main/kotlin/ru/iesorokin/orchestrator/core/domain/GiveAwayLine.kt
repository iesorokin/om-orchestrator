package ru.iesorokin.payment.orchestrator.core.domain

import java.math.BigDecimal

data class GiveAwayLine (
        val extLineId: String,
        val quantity: BigDecimal
)