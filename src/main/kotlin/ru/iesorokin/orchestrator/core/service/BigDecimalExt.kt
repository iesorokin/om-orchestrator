package ru.iesorokin.orchestrator.core.service

import java.math.BigDecimal
import java.math.RoundingMode

internal fun BigDecimal.roundTwoDigit() = this.setScale(2, RoundingMode.HALF_UP)