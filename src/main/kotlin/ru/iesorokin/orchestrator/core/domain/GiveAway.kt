package ru.iesorokin.payment.orchestrator.core.domain

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.LocalDateTime

data class GiveAway(
        val giveAwayId: String? = null, // TODO: make not null, when there will be no dependencies on processInstanceId field
        val createdBy: String,
        val created: LocalDateTime,
        val processInstanceId: String? = null,
        val lines: Collection<GiveAwayExternalLine>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GiveAwayExternalLine(
        val extLineId: String,
        val itemCode: String,
        val unitAmountIncludingVat: BigDecimal,
        val lineId: String? = null,
        val quantity: BigDecimal,
        val agencyAgreement: AgencyAgreement? = null
)
