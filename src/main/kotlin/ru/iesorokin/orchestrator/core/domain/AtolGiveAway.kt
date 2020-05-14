package ru.iesorokin.payment.orchestrator.core.domain

import ru.iesorokin.payment.orchestrator.core.enums.SupplierType
import java.math.BigDecimal
import java.time.LocalDateTime

data class AtolGiveAway(
        val taskId: String,
        val giveAwayId: String? = null,
        val correlationKey: String? = null,
        val processInstanceId: String? = null,
        val dateTime: LocalDateTime,
        val orderId: String,
        val storeId: Int,
        val payer: AtolGivePayer,
        val products: Collection<AtolGiveAwayProduct>,
        val total: BigDecimal
)

data class AtolGivePayer(
        val email: String? = null,
        val phone: String
)

data class AtolGiveAwayProduct(
        val name: String,
        val price: BigDecimal,
        val quantity: BigDecimal,
        val sum: BigDecimal,
        val tax: BigDecimal,
        val supplierInfo: SupplierInfo? = null,
        val agentInfo: SupplierType? = null
)

data class SupplierInfo(
        val inn: String?,
        val name: String?,
        val phones: Collection<String>?
)
