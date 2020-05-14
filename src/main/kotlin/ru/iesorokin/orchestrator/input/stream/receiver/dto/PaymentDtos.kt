package ru.iesorokin.payment.orchestrator.input.stream.receiver.dto

import ru.iesorokin.payment.orchestrator.core.enums.bpmn.PaymentTransactionStatus

data class PaymentTransactionMessage(val paymentTransaction: String,
                                     val status: PaymentTransactionStatus)

data class PaymentApproveMessage(val paymentTaskId: String)
