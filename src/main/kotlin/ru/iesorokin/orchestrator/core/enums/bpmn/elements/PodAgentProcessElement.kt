package ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements

import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.CALL_ACTIVITY
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.elements.ProcessElementType.SERVICE_TASK

enum class PodAgentProcessElement(val code: String, val type: ProcessElementType) {
    CHANGE_TASK_STATUS_TO_DEPOSIT_IN_PROGRESS_TASK("changeTaskStatusToDepositInProgressTask", SERVICE_TASK),
    CHANGE_TASK_STATUS_TO_GIVEAWAY_IN_PROGRESS_TASK("changeTaskStatusToGiveAwayInProgressTask", SERVICE_TASK),
    CHANGE_TASK_STATUS_TO_DEPOSIT_GIVEAWAY_DONE_TASK("changeTaskStatusToDepositGiveAwayDoneTask", SERVICE_TASK),
    CHANGE_TASK_STATUS_TO_PAID_TASK("changeTaskStatusToPaidTask", SERVICE_TASK),
    CREATE_GIVEAWAY_TASK("createGiveAwayTask", SERVICE_TASK),
    RECEIVE_PAID_STATUS_TASK("receivePaidStatusTask", SERVICE_TASK),
    TPNET_DEPOSIT_CALL_ACTIVITY("tpNetDepositCallActivity", CALL_ACTIVITY),
    TPNET_GIVEAWAY_CALL_ACTIVITY("tpNetGiveAwayCallActivity", CALL_ACTIVITY)
}