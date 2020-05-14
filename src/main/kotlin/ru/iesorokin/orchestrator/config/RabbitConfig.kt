package ru.iesorokin.payment.orchestrator.config

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.SubscribableChannel

internal const val CREATE_PAYMENT_TASK_WORKFLOW_INPUT = "createPaymentTaskWorkflowInput"
internal const val PAYMENT_TRANSACTION_STATUS_MODIFIED_INPUT = "paymentTransactionStatusModifiedInput"
internal const val CREATE_SBERBANK_DEPOSIT_TRANSACTION_OUTPUT = "createSberbankDepositTransactionOutput"
internal const val SBERBANK_TRANSACTION_DEPOSITED_INPUT = "sberbankTransactionDepositedInput"
internal const val PAYMENT_TASK_APPROVE_INPUT = "approvePaymentTaskInput"
internal const val PAYMENT_ORCHESTRATOR_COMMAND_INPUT = "paymentOrchestratorCommandInput"
internal const val START_ORCHESTRATION_PROCESS = "startOrchestrationProcessInput"
internal const val ATOL_TRANSACTION_STATUS_MODIFIED_INPUT = "atolTransactionStatusModifiedInput"
internal const val ATOL_TRANSACTION_STATUS_MODIFIED_REFUND_INPUT = "atolTransactionStatusModifiedRefundInput"
internal const val ATOL_TRANSACTION_STATUS_MODIFIED_GIVE_AWAY_INPUT = "atolTransactionStatusModifiedGiveAwayInput"
internal const val PAYMENT_START_REFUND_INPUT = "createPaymentRefundWorkflowInput"
internal const val PROCESS_TPNET_TRANSACTION_COMMAND_OUTPUT = "processTpnetTransactionCommandOutput"
internal const val TP_NET_TRANSACTION_COMMAND_FAILED_INPUT = "tpnetTransactionCommandFailedInput"
internal const val PROCESS_TPNET_REFUND_COMMAND_OUTPUT = "processTpnetRefundCommandOutput"
internal const val PROCESS_TP_NET_GIVE_AWAY_COMMAND_OUTPUT = "processTpNetGiveAwayCommandOutput"
internal const val TP_NET_REFUND_COMMAND_FAILED_INPUT = "tpnetRefundCommandFailedInput"
internal const val TP_NET_REFUND_COMMAND_SUCCESS_INPUT = "tpnetRefundCommandSuccessInput"
internal const val TP_NET_GIVE_AWAY_FAILED_INPUT = "tpnetGiveAwayFailedInput"
internal const val TP_NET_GIVE_AWAY_SUCCESSS_INPUT = "tpnetGiveAwaySuccessInput"
internal const val TP_NET_TRANSACTION_COMMAND_SUCCESSS_INPUT = "tpnetTransactionCommandSuccessInput"
internal const val CANCEL_SBERLINK_WITH_TP_NET_DEPOSIT_WORKFLOW_INPUT = "cancelSberlinkWithTpnetDepositWorkflowInput"
internal const val CANCEL_PREPAYMENT_PROCESS_INPUT = "cancelPrepaymentProcessInput"
internal const val FISCALIZATION_STATUS_OUTPUT = "fiscalizationStatusOutput"
internal const val CREATE_ITSM_TICKET_OUTPUT = "createItsmTicketOutput"
internal const val BILLING_PAID_STATUS_INPUT = "billingPaidStatusInput"
internal const val CONDUCT_BUSINESS_PROCESS_OUTPUT = "conductBusinessProcessOutput"

interface MessagingSource {
    @Input(CREATE_PAYMENT_TASK_WORKFLOW_INPUT)
    fun createPaymentTaskWorkflowInput(): SubscribableChannel

    @Input(PAYMENT_TRANSACTION_STATUS_MODIFIED_INPUT)
    fun paymentTransactionStatusModifiedInput(): SubscribableChannel

    @Input(PAYMENT_TASK_APPROVE_INPUT)
    fun approvePaymentTaskInput(): SubscribableChannel

    @Input(PAYMENT_ORCHESTRATOR_COMMAND_INPUT)
    fun paymentOrchestratorCommandInput(): SubscribableChannel

    @Output(CREATE_SBERBANK_DEPOSIT_TRANSACTION_OUTPUT)
    fun createSberbankDepositTransactionOutput(): SubscribableChannel

    @Input(SBERBANK_TRANSACTION_DEPOSITED_INPUT)
    fun sberbankTransactionDepositedInput(): SubscribableChannel

    @Input(ATOL_TRANSACTION_STATUS_MODIFIED_INPUT)
    fun atolTransactionStatusModifiedInput(): SubscribableChannel

    @Input(ATOL_TRANSACTION_STATUS_MODIFIED_REFUND_INPUT)
    fun atolTransactionStatusModifiedRefundInput(): SubscribableChannel

    @Input(ATOL_TRANSACTION_STATUS_MODIFIED_GIVE_AWAY_INPUT)
    fun atolTransactionStatusModifiedGiveAwayInput(): SubscribableChannel

    @Input(PAYMENT_START_REFUND_INPUT)
    fun paymentStartRefundInput(): SubscribableChannel

    @Output(PROCESS_TPNET_TRANSACTION_COMMAND_OUTPUT)
    fun processTpnetTransactionCommandOutput(): SubscribableChannel

    @Input(TP_NET_TRANSACTION_COMMAND_FAILED_INPUT)
    fun tpNetTransactionCommandFailedInput(): SubscribableChannel

    @Input(TP_NET_TRANSACTION_COMMAND_SUCCESSS_INPUT)
    fun tpNetTransactionCommandSuccessInput(): SubscribableChannel

    @Output(PROCESS_TPNET_REFUND_COMMAND_OUTPUT)
    fun processTpnetRefundCommandOutput(): SubscribableChannel

    @Output(PROCESS_TP_NET_GIVE_AWAY_COMMAND_OUTPUT)
    fun processTpNetGiveAwayCommandOutput(): SubscribableChannel

    @Input(TP_NET_REFUND_COMMAND_FAILED_INPUT)
    fun tpNetRefundCommandFailedInput(): SubscribableChannel

    @Input(TP_NET_REFUND_COMMAND_SUCCESS_INPUT)
    fun tpNetRefundCommandSuccessInput(): SubscribableChannel

    @Input(TP_NET_GIVE_AWAY_SUCCESSS_INPUT)
    fun tpnetGiveAwaySuccessInput(): SubscribableChannel

    @Input(TP_NET_GIVE_AWAY_FAILED_INPUT)
    fun tpnetGiveAwayFailedInput(): SubscribableChannel

    @Input(CANCEL_SBERLINK_WITH_TP_NET_DEPOSIT_WORKFLOW_INPUT)
    fun cancelSberlinkWithTpnetDepositWorkflowInput(): SubscribableChannel

    @Input(START_ORCHESTRATION_PROCESS)
    fun startOrchestrationProcessInput(): SubscribableChannel

    @Output(FISCALIZATION_STATUS_OUTPUT)
    fun fiscalizationStatusOutput(): SubscribableChannel

    @Output(CREATE_ITSM_TICKET_OUTPUT)
    fun createItsmTicketOutput(): SubscribableChannel

    @Input(BILLING_PAID_STATUS_INPUT)
    fun billingPaidStatusInput(): SubscribableChannel

    @Input(CANCEL_PREPAYMENT_PROCESS_INPUT)
    fun cancelPrepaymentProcessInput(): SubscribableChannel

    @Output(CONDUCT_BUSINESS_PROCESS_OUTPUT)
    fun conductBusinessProcessOutput(): SubscribableChannel
}