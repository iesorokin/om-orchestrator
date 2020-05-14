package ru.iesorokin.orchestrator.core.service

import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus.COMPLETED
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus.PAID
import ru.iesorokin.orchestrator.core.enums.bpmn.PaymentTransactionStatus.REFUND
import ru.iesorokin.orchestrator.core.exception.NoSuitablePaymentStatusForRefund

private val suitablePaymentStatusForRefund = setOf(PAID, REFUND, COMPLETED)

@Service
class ValidationService {
    fun checkPaymentStatusForRefund(paymentTaskId: String, taskStatus: PaymentTransactionStatus) {
        if (taskStatus !in suitablePaymentStatusForRefund) {
            throw NoSuitablePaymentStatusForRefund(paymentTaskId)
        }

    }

}