package ru.iesorokin.payment.orchestrator.output.client.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class SolutionResponse(val solutionId: String,
                            val transactionId: String? = null,
                            val pinCode: String? = null,
                            val originStore: String? = null,
                            val documentVersion: String? = null,
                            val documentFormatVersion: String? = null,
                            val creationInformation: SolutionResponseCreationInformation? = null,
                            val lastUpdateInformation: SolutionResponseLastUpdateInformation? = null,
                            val solutionStatus: String? = null,
                            val workflowId: String? = null,
                            val solutionLines: Collection<SolutionResponseLine>? = null,
                            val customers: Collection<SolutionResponseCustomer>? = null,
                            val errors: Collection<SolutionResponseError>? = null)

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class SolutionResponseCustomer(val customerNumber: String? = null,
                                    val name: String? = null,
                                    val surname: String? = null,
                                    val phone: SolutionResponsePhone? = null,
                                    val email: String? = null,
                                    val type: String? = null,
                                    val roles: Collection<String>? = null)

data class SolutionResponsePhone(val primary: String? = null,
                                 val secondary: String? = null)


data class SolutionResponseCreationInformation(val createdBy: String? = null,
                                               val created: String? = null,
                                               val channel: String? = null,
                                               val deviceType: String? = null,
                                               val frontApplication: String? = null)

data class SolutionResponseLastUpdateInformation(val updateBy: String? = null,
                                                 val updated: String? = null)


data class SolutionResponseError(val code: Int? = null,
                                 val message: String? = null)

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class SolutionResponseLine(val itemReference: String,
                                val lineId: String,
                                val longTail: Boolean? = null,
                                val stock: BigDecimal? = null,
                                val vat: Int? = null,
                                val confirmedQuantity: BigDecimal? = null,
                                val type: String? = null,
                                val price: BigDecimal? = null,
                                val quantity: BigDecimal? = null,
                                val name: String? = null,
                                val discount: SolutionResponseDiscount? = null)

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class SolutionResponseDiscount(val type: String? = null,
                                    val reason: String? = null,
                                    val typeValue: BigDecimal? = null,
                                    val actor: String? = null)
