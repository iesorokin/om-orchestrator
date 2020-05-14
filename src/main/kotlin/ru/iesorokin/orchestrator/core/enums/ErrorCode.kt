package ru.iesorokin.orchestrator.core.enums

enum class ErrorCode(val code: Int, val errorMessage: String) {
    PAYMENT_TASK_NOT_AVAILABLE(105, "Payment task is not available."),
    PAYMENT_TASK_ERROR(101, "Payment task error."),
    SOLUTION_NOT_AVAILABLE(200, "Solution is not available."),
    SOLUTION_ERROR(201, "Solution error."),
    ATOL_NOT_AVAILABLE(300, "Atol is not available."),
    ATOL_ERROR(301, "Atol error."),
    SBERBANK_NOT_AVAILABLE(400, "Sberbank is not available."),
    SBERBANK_ERROR(401, "Sberbank error."),
    SMS_NOT_AVAILABLE(501, "Sms service is not available."),
    SMS_ERROR(502, "Sms service error."),
    PAYMENT_TASK_NOT_FOUND(100, "Task not found."),
    LINE_TYPE_ERROR(101, "Editing of unitAmountIncludingVat is available only for delivery lines."),
    VAT_ERROR(102, "'unitAmountIncludingVat' must be less or equal than current value and not negative."),
    TASK_STATUS_ERROR(103, "Editing of 'unitAmountIncludingVat' is available only for " +
            "SBERLINK_WITH_TPNET_DEPOSIT with payment status HOLD or for POD_AGENT/POD_POST_PAYMENT with payment status CONFIRMED."),
    LINE_NOT_EXIST(104, "Input extLineId does not exist in payment task."),
    FAILED_START_PROCESS(107, "Failed to start process."),
}
