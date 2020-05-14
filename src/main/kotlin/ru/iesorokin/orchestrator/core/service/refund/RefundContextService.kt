package ru.iesorokin.orchestrator.core.service.refund

import org.springframework.stereotype.Component
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.domain.PaymentTaskLine
import ru.iesorokin.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.orchestrator.core.domain.PaymentTaskRegisterStatusLine
import ru.iesorokin.orchestrator.core.domain.RefundLine
import ru.iesorokin.orchestrator.core.enums.PaymentTaskLineType
import java.math.BigDecimal
import java.math.RoundingMode

@Component
class RefundContextService {

    fun buildRefundLines(paymentTask: PaymentTask): List<RefundLine> {
        val extLineIdToPaymentTaskRegisterStatusLine = paymentTask.registerStatus
                .getExtLineIdToPaymentTaskRegisterStatusLine()
        return paymentTask.lines
                .asSequence()
                .filter { it.isNeedAddLineToRefund(extLineIdToPaymentTaskRegisterStatusLine?.get(it.extLineId)) }
                .map { it.buildRefundLine(extLineIdToPaymentTaskRegisterStatusLine?.get(it.extLineId)) }
                .toList()
    }

}

private fun PaymentTaskRegisterStatus?.getExtLineIdToPaymentTaskRegisterStatusLine() = this?.lines
        ?.map { it.extLineId to it }
        ?.toMap()


private fun PaymentTaskLine.isNeedAddLineToRefund(registerStatusLine: PaymentTaskRegisterStatusLine?): Boolean {
    if ((!isDeliveryLine() && quantity <= confirmedQuantity) || (isDeliveryLine() && calculateUnitAmountIncludingVatForDeliveryLine(registerStatusLine) <= BigDecimal.ZERO)) {
        return false
    }
    return true
}

private fun PaymentTaskLine.isDeliveryLine(): Boolean {
    if (lineType != null && lineType == PaymentTaskLineType.DELIVERY.name) {
        return true
    }
    return false
}

private fun PaymentTaskLine.calculateUnitAmountIncludingVatForDeliveryLine(registerStatusLine: PaymentTaskRegisterStatusLine?): BigDecimal {
    val minuend = registerStatusLine?.unitAmountIncludingVat?.multiply(registerStatusLine.quantity)?.round()
            ?: BigDecimal.ZERO
    val subtrahend = (unitAmountIncludingVat * confirmedQuantity).round()
    return (minuend - subtrahend).round()
}

private fun PaymentTaskLine.buildRefundLine(registerStatusLine: PaymentTaskRegisterStatusLine?): RefundLine {
    if (isDeliveryLine()) {
        return createDeliveryRefundLine(registerStatusLine)
    }
    return createRefundLine()
}

private fun PaymentTaskLine.createDeliveryRefundLine(registerStatusLine: PaymentTaskRegisterStatusLine?) = RefundLine(
        extLineId = extLineId!!,
        quantity = BigDecimal.ONE,
        unitAmountIncludingVat = calculateUnitAmountIncludingVatForDeliveryLine(registerStatusLine)
)

private fun PaymentTaskLine.createRefundLine() = RefundLine(
        extLineId = extLineId!!,
        quantity = quantity - confirmedQuantity,
        unitAmountIncludingVat = unitAmountIncludingVat
)

private fun BigDecimal.round() = this.setScale(2, RoundingMode.HALF_UP)
