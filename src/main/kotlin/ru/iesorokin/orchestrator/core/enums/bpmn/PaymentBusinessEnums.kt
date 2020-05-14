package ru.iesorokin.payment.orchestrator.core.enums.bpmn

import org.camunda.bpm.engine.delegate.BpmnError

enum class PaymentTransactionStatus {
    NEW,
    CONFIRMED,
    HOLD,
    UNHOLD,
    PAID,
    DEPOSIT_IN_PROGRESS,
    COMPLETED,
    REFUND,
    PARTIALLY_REFUND,
    EXPIRED,
    CANCELLED,
    APPROVE_IN_PROGRESS,
    DEPOSIT_AND_GIVEAWAY_DONE,
    GIVEAWAY_IN_PROGRESS
}

enum class BusinessProcessEvent(val message: String) {
    PAYMENT_RECEIVED("sberbankPaymentReceived"),
    PAYMENT_EXPIRED("sberbankPaymentExpired"),
    PAYMENT_APPROVED("approvePayment"),
    PAYMENT_COMPLETE("receiveTaskCompletion"),
    ATOL_REGISTER_SUCCESS("atolRegisterSuccess"),
    SBERBANK_DEPOSIT_SUCCESS("receiveSberbankConfirmationSuccess"),
    SBERBANK_DEPOSIT_FAIL("receiveSberbankConfirmationFail"),
    TPNET_DEPOSIT_SUCCESS("tpnetDepositSuccess"),
    TPNET_DEPOSIT_FAIL("tpnetDepositFail"),
    ATOL_REFUND_SUCCESS("atolRefundSuccess"),
    ATOL_GIVE_AWAY_SUCCESS("atolGiveAwayFiscal"),
    CANCEL_PREPAYMENT_PROCESS("cancelPrepayment"),
    TPNET_REFUND_SUCCESS("tpnetRefundSuccess"),
    TPNET_REFUND_FAIL("tpnetRefundFail"),
    TPNET_GIVE_AWAY_SUCCESS("tpnetGiveAwaySuccess"),
    TPNET_GIVE_AWAY_FAIL("tpnetGiveAwayFail"),
    BILLING_PAID_STATUS("billingPaidStatus")
}

enum class BoundaryEventType(val eventName: String) {
    EXPIRED_PAYMENT_TASK_MESSAGE("expiredPaymentTaskMessage")
}

enum class BpmnErrors {
    PAYMENT_NOT_FOUND,
    REFUND_SBERBANK_ERROR,
    STATUS_CHECK_ERROR;

    fun toBpmnError() = BpmnError(name, name)
    fun toBpmnError(e: Throwable) = BpmnError(name, name + " - " + (e.message ?: "unexpected error"))

}

enum class BusinessProcessCounters(val counterName: String) {
    CHECK_CURRENT_STATUS_COUNT("checkCurrentStatusCount")
}

enum class DepositEventType { FAIL, SUCCESS }
enum class RefundEventType { FAIL, SUCCESS }
