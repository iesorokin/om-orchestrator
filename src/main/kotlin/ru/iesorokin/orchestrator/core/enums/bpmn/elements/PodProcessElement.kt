package ru.iesorokin.orchestrator.core.enums.bpmn.elements

import ru.iesorokin.orchestrator.core.enums.bpmn.elements.ProcessElementType.CALL_ACTIVITY
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.ProcessElementType.RECEIVE_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.ProcessElementType.SEND_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.ProcessElementType.SERVICE_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.ProcessElementType.USER_TASK

enum class PodProcessElement(val code: String, val type: ProcessElementType) {
    CHANGE_TASK_STATUS_TO_APPROVE_IN_PROGRESS("changePaymentTaskStatusToApproveInProgressTask", SERVICE_TASK),
    @Deprecated("delete after 01.05.2020 - useless")
    REGISTER_ATOL_TRANSACTION("registerAtolTransactionTask", SERVICE_TASK),
    REGISTER_TRANSACTION_IN_ATOL_TASK("registerTransactionInAtolTask", SERVICE_TASK),
    UPDATE_TASK_BY_REGISTERED_TRANSACTION_TASK("updateTaskByRegisteredTransactionTask", SERVICE_TASK),
    RECEIVE_ATOL_SUCCESS("receiveAtolSuccess", RECEIVE_TASK),
    SAVE_DATA_FROM_ATOL("saveDataFromAtolTask", SERVICE_TASK),
    CHANGE_TASK_STATUS_TO_DEPOSIT_IN_PROGRESS("changePaymentTaskStatusToDepositInProgressTask", SERVICE_TASK),
    PLACE_TP_NET_DEPOSIT("placeTpNetDepositTask", SEND_TASK),
    RECEIVE_TP_NET_DEPOSIT_SUCCESS("receiveTpnetDepositSuccess", RECEIVE_TASK),
    RECEIVE_TP_NET_DEPOSIT_FAIL("receiveTpNetDepositFail", RECEIVE_TASK),
    TRY_PLACE_DEPOSIT("tryPlaceDepositTask", USER_TASK),
    CHANGE_TASK_STATUS_TO_PAID("changePaymentTaskStatusToPaidTask", SERVICE_TASK),
    CREATE_GIVE_AWAY("createPaymentGiveAwayTask", SERVICE_TASK),
    SEND_STARTED_FISCALIZATION_STATUS_TASK("sendStartedFiscalizationStatusTask", SEND_TASK),
    SEND_FINISHED_FISCALIZATION_STATUS_TASK("sendFinishedFiscalizationStatusTask", SEND_TASK),
    RUN_GIVEAWAY_PROCESS_ACTIVITY("runGiveAwayProcessActivity", CALL_ACTIVITY)
}