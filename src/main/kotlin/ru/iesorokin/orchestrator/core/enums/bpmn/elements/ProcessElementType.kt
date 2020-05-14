package ru.iesorokin.orchestrator.core.enums.bpmn.elements

enum class ProcessElementType {
    TASK,
    USER_TASK,
    SEND_TASK,
    SERVICE_TASK,
    SCRIPT_TASK,
    BOUNDARY_EVENT,
    CONDITIONAL_EVENT,
    TIMER_EVENT,
    RECEIVE_TASK,
    EXCLUSIVE_GATEWAY,
    ESCALATION,
    GATEWAY,
    CALL_ACTIVITY
}
