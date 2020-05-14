package ru.iesorokin.payment.orchestrator.output.client

import java.math.BigDecimal
import java.math.RoundingMode

fun List<BigDecimal>.sum(): BigDecimal =
        this.fold(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP)
