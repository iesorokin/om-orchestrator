package ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements

import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.*

enum class RefundProcessElement(val code: String, val type: ProcessElementType) {
    REGISTER_REFUND_ATOL_TRANSACTION_TASK("registerRefundAtolTransactionServiceTask", SERVICE_TASK),
    REFUND_SBERBANK_PAYMENT_TASK("refundSberbankPaymentTask", SERVICE_TASK),
    REFUND_TP_NET_TASK("refundTpNetTask", SEND_TASK),
    RECEIVE_TP_NET_REFUND_SUCCESS_TASK("receiveRefundTpNetSuccessTask", RECEIVE_TASK),
    RECEIVE_ATOL_REFUND_SUCCESS_RECEIVE_TASK("receiveAtolRefundSuccessTask", RECEIVE_TASK),
    RECEIVE_TP_NET_REFUND_FAIL_TASK("receiveRefundTpNetFailTask", RECEIVE_TASK),
    SAVE_FISCAL_DATA_TASK("saveFiscalDataTask", SERVICE_TASK),
    CHECK_CURRENT_PAYMENT_STATUS_TASK("checkCurrentPaymentStatusTask", SERVICE_TASK),
    SOLVE_PROBLEM_SBERBANK("solveProblemRefundSberbankUserTask", USER_TASK),
    SOLVE_PROBLEM_TPNET("solveProblemRefundTpNetUserTask", USER_TASK),
}