package ru.iesorokin.orchestrator.core.domain

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.ZonedDateTime

const val LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss[.SSS]"

data class PaymentTask(
        val buCode: String? = null,
        val channel: String? = null,
        val taskId: String,
        val taskStatus: String,
        val extOrderId: String? = null,
        val creationInformation: CreationInformation? = null,
        val taskType: String,
        val workflowId: String? = null,
        val executionStore: Int? = null,
        val links: Collection<PaymentTaskLink>? = null,
        val pinCode: String? = null,
        val lines: Collection<PaymentTaskLine>,
        var refundStatusList: Collection<PaymentTaskFiscalData>? = null,
        val registerStatus: PaymentTaskRegisterStatus? = null,
        val extLineIdToAgencyAgreement: Map<String, AgencyAgreement>? = null
)

data class PaymentTaskLink(
        val type: String,
        val link: String
)

data class PaymentTaskLine(
        val lineStatus: String,
        val lineType: String? = null,
        val itemCode: String,
        val itemName: String? = null,
        val quantity: BigDecimal,
        val unitAmountIncludingVat: BigDecimal,
        val taxRate: BigDecimal? = null,
        val unitVatAmount: BigDecimal? = null,
        val extLineId: String? = null,
        val confirmedQuantity: BigDecimal,
        val depositQuantity: BigDecimal,
        val discount: PaymentTaskLineDiscount? = null
)

data class PaymentTaskRegisterStatus(
        var atolId: String? = null,
        var uuid: String? = null,
        var ecrRegistrationNumber: String? = null,
        var fiscalDocumentNumber: Long? = null,
        var fiscalStorageNumber: String? = null,
        var status: String? = null,
        val lines: Collection<PaymentTaskRegisterStatusLine>? = null
)

data class PaymentTaskRegisterStatusLine(
        val extLineId: String,
        val quantity: BigDecimal,
        val unitAmountIncludingVat: BigDecimal
)

data class PaymentTaskFiscalData(
        val created: ZonedDateTime? = null,
        var atolId: String? = null,
        var status: String? = null,
        var uuid: String? = null,
        var ecrRegistrationNumber: String? = null,
        var fiscalDocumentNumber: Long? = null,
        var fiscalStorageNumber: String? = null,
        var refundWorkflowId: String? = null,
        val lines: Collection<PaymentTaskFiscalDataLine>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentTaskFiscalDataLine(
        val extLineId: String,
        var lineId: String,
        val quantity: BigDecimal,
        val unitAmountIncludingVat: BigDecimal? = null
)

data class PaymentTaskLineDiscount(
        val type: String,
        val typeValue: BigDecimal
)

enum class LineType { PRODUCT, SERVICE, DELIVERY }

enum class PaymentTaskStatus { HOLD, CONFIRMED }
