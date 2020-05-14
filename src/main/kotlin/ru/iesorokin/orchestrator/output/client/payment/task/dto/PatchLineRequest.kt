package ru.iesorokin.payment.orchestrator.output.client.payment.task.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ru.iesorokin.payment.orchestrator.config.jackson.MoneyBigDecimalDeserializer
import java.math.BigDecimal
import javax.validation.Valid

data class PatchLinesRequest(
        val updateBy: String,
        @field:Valid
        val lines: Collection<PatchLineRequest>
)

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class PatchLineRequest(

        val extLineId: String? = null,

        @JsonDeserialize(using = MoneyBigDecimalDeserializer::class)
        val unitAmountIncludingVat: BigDecimal? = null,

        @JsonDeserialize(using = MoneyBigDecimalDeserializer::class)
        val confirmedQuantity: BigDecimal? = null

)