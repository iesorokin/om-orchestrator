package ru.iesorokin.payment.orchestrator.output.client.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import ru.iesorokin.payment.orchestrator.config.jackson.TaxBigDecimalSerializer
import ru.iesorokin.payment.orchestrator.core.enums.SupplierType
import java.math.BigDecimal
import java.time.ZonedDateTime

const val ZONED_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss[.SSS]xxx"

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class AtolRegisterRequest(
        val taskId: String,
        val processInstanceId: String? = null, //todo: delete after 01.04.2020 - useless variable
        val correlationKey: String? = null,
        @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_PATTERN)
        val dateTime: ZonedDateTime,
        val orderId: String,
        val storeId: Int,
        val payer: AtolRegisterRequestPayer,
        val products: List<AtolRegisterRequestProduct>,
        val total: BigDecimal
)

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class AtolGiveAwayRegisterRequest(
        val taskId: String,
        val giveAwayId: String? = null,
        val processInstanceId: String? = null, //todo: delete after 01.04.2020 - useless variable
        val correlationKey: String? = null,
        @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ZONED_DATE_TIME_PATTERN)
        val dateTime: ZonedDateTime,
        val orderId: String,
        val storeId: Int,
        val payer: AtolRegisterRequestPayer,
        val products: List<AtolRegisterRequestProduct>,
        val total: BigDecimal
)

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class AtolRegisterRequestPayer(
        val email: String? = null,
        val phone: String
)

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class AtolRegisterRequestProduct(
        val name: String,
        val price: BigDecimal,
        val quantity: BigDecimal,
        val sum: BigDecimal,
        @JsonSerialize(using = TaxBigDecimalSerializer::class)
        val tax: BigDecimal,
        val supplierInfo: AtolRegisterRequestSupplier? = null,
        val agentInfo: AtolRegisterRequestAgentInfo? = null
)

data class AtolRegisterRequestAgentInfo(
        val type: SupplierType?
)

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class AtolRegisterRequestSupplier(
        val name: String?,
        val inn: String?,
        val phones: Collection<String>? = null
)

data class AtolRegisterResponse(val atolId: String)
