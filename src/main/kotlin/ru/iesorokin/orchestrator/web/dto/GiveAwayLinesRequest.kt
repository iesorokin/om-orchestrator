package ru.iesorokin.orchestrator.web.dto

import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class GiveAwayLinesRequest(val createdBy: String,
                                @field:Valid
                                val lines: Collection<GiveAwayLineRequest>)

data class GiveAwayLineRequest(val extLineId: String,
                               @field:[NotNull Positive]
                               val quantity: BigDecimal)
