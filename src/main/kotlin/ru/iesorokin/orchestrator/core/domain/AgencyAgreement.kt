package ru.iesorokin.orchestrator.core.domain

import com.fasterxml.jackson.annotation.JsonProperty
import ru.iesorokin.orchestrator.core.enums.SupplierType
import java.math.BigDecimal
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin

data class AgencyAgreement (
        // todo: rename everywhere extLineId -> extLineIds
        @JsonProperty("extLineId")
        val extLineIds: Collection<String>? = null,
        val supplierName: String? = null,
        val supplierInn: String? = null,
        val supplierType: SupplierType? = null,
        @field:[DecimalMin("0") DecimalMax("100")]
        val supplierTaxRate: BigDecimal? = null,
        @JsonProperty("supplierPhone")
        val supplierPhones: Collection<String>? = null
)