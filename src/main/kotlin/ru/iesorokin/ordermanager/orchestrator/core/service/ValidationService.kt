package ru.iesorokin.ordermanager.orchestrator.core.service

import org.springframework.stereotype.Service
import ru.iesorokin.ordermanager.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.ordermanager.orchestrator.core.enums.bpmn.PaymentTransactionStatus.COMPLETED
import ru.iesorokin.ordermanager.orchestrator.core.enums.bpmn.PaymentTransactionStatus.PAID
import ru.iesorokin.ordermanager.orchestrator.core.enums.bpmn.PaymentTransactionStatus.REFUND
import ru.iesorokin.ordermanager.orchestrator.core.exception.NoSuitablePaymentStatusForRefund

private val suitablePaymentStatusForRefund = setOf(PAID, REFUND, COMPLETED)

@Service
class ValidationService {
    fun checkPaymentStatusForRefund(paymentTaskId: String, taskStatus: PaymentTransactionStatus) {
        if (taskStatus !in suitablePaymentStatusForRefund) {
            throw NoSuitablePaymentStatusForRefund(paymentTaskId)
        }

    }

}