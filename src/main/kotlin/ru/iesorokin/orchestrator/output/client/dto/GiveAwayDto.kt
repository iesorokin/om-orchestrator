package ru.iesorokin.orchestrator.output.client.dto

import com.fasterxml.jackson.annotation.JsonFormat
import ru.iesorokin.orchestrator.core.domain.LOCAL_DATE_TIME_PATTERN
import java.math.BigDecimal
import java.time.LocalDateTime

data class GiveAwayResponseItem(
        val createdBy: String,
        @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = LOCAL_DATE_TIME_PATTERN)
        val created: LocalDateTime,
        val processInstanceId: String? = null,
        val lines: Collection<GiveAwayResponseLine>
)

data class GiveAwayResponseLine(
        val extLineId: String,
        val itemCode: String,
        val unitAmountIncludingVat: BigDecimal,
        val lineId: String,
        val quantity: BigDecimal
)