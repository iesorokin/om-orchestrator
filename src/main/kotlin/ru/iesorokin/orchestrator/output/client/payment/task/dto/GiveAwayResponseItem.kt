package ru.iesorokin.orchestrator.output.client.payment.task.dto

import com.fasterxml.jackson.annotation.JsonFormat
import ru.iesorokin.orchestrator.core.enums.SupplierType
import java.math.BigDecimal
import java.time.LocalDateTime

private const val LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS"

data class GiveAwayResponseItem(
        val createdBy: String,
        @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = LOCAL_DATE_TIME_PATTERN)
        val created: LocalDateTime,
        val processInstanceId: String? = null,
        val giveAwayId: String? = null,
        val lines: Collection<GiveAwayResponseItemLine>
)

data class GiveAwayResponseItemLine(
        val extLineId: String,
        val itemCode: String,
        val unitAmountIncludingVat: BigDecimal,
        val lineId: String,
        val quantity: BigDecimal,
        val agencyAgreement: AgencyAgreementResponse? = null
)

data class AgencyAgreementResponse(
        val supplierName: String? = null,
        val supplierInn: String? = null,
        val supplierType: SupplierType? = null,
        val supplierTaxRate: BigDecimal? = null,
        val supplierPhone: Collection<String>? = null
)