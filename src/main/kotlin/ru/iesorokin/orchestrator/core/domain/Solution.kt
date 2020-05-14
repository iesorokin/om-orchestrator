package ru.iesorokin.payment.orchestrator.core.domain

import java.math.BigDecimal

data class Solution(
        val solutionId: String,
        val transactionId: String? = null,
        val pinCode: String? = null,
        val originStore: String? = null,
        val documentVersion: String? = null,
        val documentFormatVersion: String? = null,
        val creationInformation: CreationInformation? = null,
        val lastUpdateInformation: LastUpdateInformation? = null,
        val solutionStatus: String? = null,
        val workflowId: String? = null,
        val solutionLines: Collection<SolutionLine>? = null,
        val customers: Collection<SolutionCustomer>? = null
)

data class CreationInformation(
        val createdBy: String? = null,
        val created: String? = null,
        val channel: String? = null,
        val deviceType: String? = null,
        val frontApplication: String? = null
)

data class LastUpdateInformation(
        val updateBy: String? = null,
        val updated: String? = null
)

data class SolutionLine(
        val itemReference: String,
        val lineId: String,
        val longTail: Boolean? = null,
        val stock: BigDecimal? = null,
        val vat: Int? = null,
        val confirmedQuantity: BigDecimal? = null,
        val type: String? = null,
        val price: BigDecimal? = null,
        val quantity: BigDecimal? = null,
        val name: String? = null,
        val discount: SolutionDiscount? = null
)

data class SolutionCustomer(
        val customerNumber: String? = null,
        val name: String? = null,
        val surname: String? = null,
        val phone: Phone? = null,
        val email: String? = null,
        val type: String? = null,
        val roles: Collection<String>? = null
)

data class Phone(
        val primary: String? = null,
        val secondary: String? = null
)

data class SolutionDiscount(
        val type: String? = null,
        val reason: String? = null,
        val typeValue: BigDecimal? = null,
        val actor: String? = null
)

