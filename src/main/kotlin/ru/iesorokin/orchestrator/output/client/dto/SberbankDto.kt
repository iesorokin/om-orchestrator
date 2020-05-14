package ru.iesorokin.payment.orchestrator.output.client.dto

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import ru.iesorokin.payment.orchestrator.config.jackson.MoneyBigDecimalSerializer
import java.math.BigDecimal

data class SberbankRefundRequest(val storeId: Int,
                                 @JsonSerialize(using = MoneyBigDecimalSerializer::class)
                                 val refundAmount: BigDecimal)