package ru.iesorokin.orchestrator.core.enums.bpmn.elements

import ru.iesorokin.orchestrator.core.enums.bpmn.elements.ProcessElementType.EXCLUSIVE_GATEWAY
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.ProcessElementType.RECEIVE_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.ProcessElementType.SEND_TASK
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.ProcessElementType.TIMER_EVENT
import ru.iesorokin.orchestrator.core.enums.bpmn.elements.ProcessElementType.USER_TASK

enum class TpNetInteractionProcessElement(val code: String, val type: ProcessElementType) {
    PLACE_TP_NET_DEPOSIT("placeTpNetDepositTask", SEND_TASK),
    PLACE_TP_NET_GIVEAWAY("placeTpNetGiveAwayTask", SEND_TASK),
    RECEIVE_TP_NET_DEPOSIT_SUCCESS("receiveTpNetDepositSuccessTask", RECEIVE_TASK),
    RECEIVE_TP_NET_GIVEAWAY_SUCCESS("receiveTpNetGiveAwaySuccessTask", RECEIVE_TASK),
    SOLVE_ISSUE_PLACE_DEPOSIT("tpNetDepositHumanTask", USER_TASK),
    SOLVE_ISSUE_PLACE_GIVEAWAY("tpNetGiveAwayHumanTask", USER_TASK),
    CREATE_ITSM_TICKET_DEPOSIT("createTpNetDepositItsmTicketTask", SEND_TASK),
    CREATE_ITSM_TICKET_GIVEAWAY("createTpNetGiveAwayItsmTicketTask", SEND_TASK),
    RESOLVE_SEQUENCE("resolveSequence", EXCLUSIVE_GATEWAY),
    RECIEVE_TP_NET_SUCCESS_GIVEAWAY_TIMER("receiveTpNetSuccessGiveAwayTimer", TIMER_EVENT),
    RECIEVE_TP_NET_SUCCESS_DEPOSIT_TIMER("receiveTpNetSuccessDepositTimer", TIMER_EVENT)
}