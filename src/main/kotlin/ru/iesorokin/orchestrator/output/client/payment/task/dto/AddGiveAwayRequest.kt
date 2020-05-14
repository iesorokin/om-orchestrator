package ru.iesorokin.orchestrator.output.client.payment.task.dto

import com.fasterxml.jackson.annotation.JsonInclude
import ru.iesorokin.orchestrator.core.domain.AgencyAgreement
import java.math.BigDecimal

data class AddGiveAwayRequest(
        val giveAwayId: String,
        val createdBy: String,
        val lines: Collection<AddGiveAwayLineRequest>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AddGiveAwayLineRequest(
        val extLineId: String,
        val itemCode: String,
        val unitAmountIncludingVat: BigDecimal,
        val quantity: BigDecimal,
        val agencyAgreement: AgencyAgreement? = null
)