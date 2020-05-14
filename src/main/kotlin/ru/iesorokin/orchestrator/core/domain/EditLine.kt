package ru.iesorokin.payment.orchestrator.core.domain

import java.math.BigDecimal

data class EditLine(
        val extLineId: String,
        val unitAmountIncludingVat: BigDecimal? = null,
        val confirmedQuantity: BigDecimal? = null)