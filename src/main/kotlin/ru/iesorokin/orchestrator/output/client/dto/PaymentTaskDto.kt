package ru.iesorokin.orchestrator.output.client.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import ru.iesorokin.orchestrator.core.domain.AgencyAgreement
import java.math.BigDecimal
import java.time.ZonedDateTime

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class PaymentTaskResponse(val buCode: String? = null,
                               val channel: String? = null,
                               val taskId: String,
                               val taskStatus: String,
                               val extOrderId: String? = null,
                               val workflowId: String? = null,
                               val creationInformation: PaymentTaskResponseCreated? = null,
                               val taskType: String,
                               val executionStore: Int? = null,
                               val links: Collection<PaymentTaskResponseLink>? = null,
                               val pinCode: String? = null,
                               val lines: Collection<PaymentTaskResponseLine>,
                               val refundStatusList: Collection<PaymentTaskResponseFiscalData>? = null,
                               val registerStatus: PaymentTaskResponseRegisterStatus? = null,
                               val errors: Collection<PaymentTaskResponseError>? = null,
                               @JsonProperty("agencyAgreement")
                               val agencyAgreements: Collection<AgencyAgreement>? = null)


@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class PaymentTaskResponseCreated(val createdBy: String? = null,
                                      val created: String? = null)

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class PaymentTaskResponseLink(val type: String,
                                   val link: String)

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class PaymentTaskResponseLine(val lineStatus: String,
                                   val itemCode: String,
                                   val lineType: String? = null,
                                   val itemName: String? = null,
                                   val quantity: BigDecimal,
                                   val unitAmountIncludingVat: BigDecimal,
                                   val taxRate: BigDecimal? = null,
                                   val unitVatAmount: BigDecimal? = null,
                                   val extLineId: String? = null,
                                   val confirmedQuantity: BigDecimal,
                                   val depositQuantity: BigDecimal,
                                   val discount: PaymentTaskResponseLineDiscount? = null)

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class PaymentTaskResponseError(val code: Int? = null,
                                    val message: String? = null)

data class PaymentTaskResponseRegisterStatus(
        var atolId: String? = null,
        var uuid: String? = null,
        var ecrRegistrationNumber: String? = null,
        var fiscalDocumentNumber: Long? = null,
        var fiscalStorageNumber: String? = null,
        var status: String? = null,
        val lines: Collection<PaymentTaskResponseRegisterStatusLine>? = null
)

data class PaymentTaskResponseRegisterStatusLine(
        val extLineId: String,
        val quantity: BigDecimal,
        val unitAmountIncludingVat: BigDecimal
)

data class PaymentTaskResponseFiscalData(
        @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_PATTERN)
        val created: ZonedDateTime? = null,
        var atolId: String? = null,
        var status: String? = null,
        val uuid: String? = null,
        val ecrRegistrationNumber: String? = null,
        val fiscalDocumentNumber: Long? = null,
        val fiscalStorageNumber: String? = null,
        val refundWorkflowId: String? = null,
        val lines: Collection<PaymentTaskResponseFiscalDataLine>? = null
)

data class PaymentTaskResponseFiscalDataLine(
        val extLineId: String,
        var lineId: String,
        val quantity: BigDecimal,
        val unitAmountIncludingVat: BigDecimal? = null
)

data class PaymentTaskResponseLineDiscount(
        val type: String,
        val typeValue: BigDecimal
)
