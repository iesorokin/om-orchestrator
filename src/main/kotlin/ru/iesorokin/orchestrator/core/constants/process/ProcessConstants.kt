package ru.iesorokin.payment.orchestrator.core.constants.process

//TODO get rid of all constants, make up enums
const val PAYMENT_TASK_ID = "paymentTaskId"
const val PAYMENT_TASK_STATUS = "paymentTaskStatus"
const val EXT_ORDER_ID = "extOrderId"
const val EXECUTION_STORE = "executionStore"
const val GIVE_AWAY_ID = "giveAwayId"
const val TP_NET_OPERATION_TYPE = "tpnetOperationType"

const val CURRENT_PAYMENT_STATUS = "currentPaymentStatus"

const val ATOL_REGISTER_ID = "atolRegisterId"
const val ATOL_REFUND_ID = "atolRefundId"
const val ATOL_GIVE_AWAY_ID = "atolGiveAwayId"
const val ATOL_REGISTER_UUID = "atolRegister.uuid"
const val ATOL_REGISTER_REGISTRATION_NUMBER = "atolRegister.ecrRegistrationNumber"
const val ATOL_REGISTER_DOCUMENT_NUMBER = "atolRegister.fiscalDocumentNumber"
const val ATOL_REGISTER_STORAGE_NUMBER = "atolRegister.fiscalStorageNumber"
const val ATOL_REGISTER_STATUS = "atolRegister.status"
const val ATOL_REFUND_UUID = "atolRefund.uuid"
const val ATOL_REFUND_REGISTRATION_NUMBER = "atolRefund.ecrRegistrationNumber"
const val ATOL_REFUND_DOCUMENT_NUMBER = "atolRefund.fiscalDocumentNumber"
const val ATOL_REFUND_STORAGE_NUMBER = "atolRefund.fiscalStorageNumber"

@Deprecated("Use Process.SBERBANK_PREPAYMENT_WITH_TPNET instead")
const val SBERBANK_PREPAYMENT_WITH_TPNET = "SBERBANK_PREPAYMENT_WITH_TPNET"

const val GIVE_AWAY = "give_away"

const val FULL_APPROVE_KEY = "fullApprove"

const val PERSON = "PERSON"

const val FISCALIZATION_STATUS = "fiscalizationStatus"

