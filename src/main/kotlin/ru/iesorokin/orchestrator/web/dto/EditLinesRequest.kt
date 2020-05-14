package ru.iesorokin.payment.orchestrator.web.dto

import java.math.BigDecimal

data class EditLinesRequest(
        val updateBy: String,
        val lines: Collection<EditLineRequest>
)

data class EditLineRequest(
        val extLineId: String,
        val unitAmountIncludingVat: BigDecimal? = null,
        val confirmedQuantity: BigDecimal? = null)