package ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements

import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.BOUNDARY_EVENT
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.GATEWAY
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.RECEIVE_TASK
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.SCRIPT_TASK
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.SEND_TASK
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.SERVICE_TASK
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.TASK
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.TIMER_EVENT
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.USER_TASK

// todo: seperate to enums
enum class BusinessProcessElement(val code: String, val type: ProcessElementType) {
    // ---------- PRE PAYMENT ----------
    SAVE_WORKFLOW_ID_TASK("saveWorkflowIdTask", SERVICE_TASK),
    @Deprecated("delete after 01.05.2020 - useless")
    REGISTER_ATOL_TRANSACTION_TASK("registerAtolTransactionServiceTask", SERVICE_TASK),
    REGISTER_TRANSACTION_IN_ATOL_TASK("registerTransactionInAtolTask", SERVICE_TASK),
    UPDATE_TASK_BY_REGISTERED_TRANSACTION_TASK("updateTaskByRegisteredTransactionTask", SERVICE_TASK),
    PAYMENT_TASK_EXPIRED("paymentTaskExpired", SERVICE_TASK),
    PAYMENT_TASK_HOLD("paymentTaskHold", SERVICE_TASK),
    PAYMENT_STATUS_DEPOSIT_IN_PROGRESS("paymentStatusDepositInProgressTask", SERVICE_TASK),
    REFUND_PROCESS("startRefundProcessTask", SERVICE_TASK),
    CHANGE_PAYMENT_STATUS_PAID_TASK("changePaymentStatusPaidTask", SERVICE_TASK),
    SAVE_DATA_FROM_ATOL_TASK("saveDataFromAtolServiceTask", SERVICE_TASK),

    RECEIVE_PAYMENT_TASK("receivePaymentTask", RECEIVE_TASK),
    RECEIVE_ATOL_SUCCESS_RECEIVE_TASK("receiveAtolSuccessTask", RECEIVE_TASK),
    RECEIVE_ATOL_GIVE_AWAY_REGISTER_SUCCESS_TASK("receiveAtolGiveAwayRegisterSuccessTask", RECEIVE_TASK),
    APPROVE_PAYMENT_TASK("approvePaymentTask", RECEIVE_TASK),
    RECEIVE_PAYEMNT_TASK_COMPLETION("receivePayemntTaskCompletion", RECEIVE_TASK),
    RECEIVE_SBERBANK_CONFIRMATION_SUCCESS_EVENT("receiveSberbankConfirmationSuccessEvent", RECEIVE_TASK),
    RECEIVE_SBERBANK_CONFIRMATION_FAIL_EVENT("receiveSberbankConfirmationFailEvent", RECEIVE_TASK),

    RECEIVE_TPNET_DEPOSIT_SUCCESS_EVENT("receiveTpNetDepositSuccessEvent", RECEIVE_TASK),
    RECEIVE_TPNET_DEPOSIT_FAIL_EVENT("receiveTpNetDepositFailEvent", RECEIVE_TASK),
    CREATE_TPNET_ITSM_TICKET_TASK("createTpnetItsmTicketTask", SEND_TASK),

    RECEIVE_PREPAYMENT_FULL_CANCELLATION_EVENT("receivePrepaymentFullCancellationEvent", BOUNDARY_EVENT),

    CONFIRM_SBERBANK_TASK("confirmSberbankTask", SEND_TASK),
    PLACE_TPNET_DEPOSIT_TASK("placeTpnetDepositTask", SEND_TASK),

    APPROVE_IN_PROGRESS_TASK("approveInProgressTask", TASK),
    CANCEL_PREPAYMENT_TASK("cancelPrepaymentTask", TASK),

    END_THE_PROCESS_TASK("endTheProcessTask", SCRIPT_TASK),

    /* ---------- GIVE AWAY ---------- */
    REGISTER_ATOL_GIVE_AWAY_TASK("registerAtolGiveAwayTask", SERVICE_TASK),

    ONE_HOUR_TIMER("oneHourTimer", TIMER_EVENT),

    SEND_REFUND_SMS_TASK("sendRefundSmsTask", SERVICE_TASK),
    TP_NET_GIVE_AWAY_TASK("tpNetGiveAwayTask", SEND_TASK),

    /* ---------- UNIFIED PREPAYMENT ---------- */
    RECEIVE_PAYMENT_GATEWAY("receivePaymentGateway", GATEWAY)
}
