package ru.iesorokin.orchestrator.core.enums.bpmn

enum class Process(val processName: String) {
    SBERBANK_PREPAYMENT_WITH_TPNET("SBERBANK_PREPAYMENT_WITH_TPNET"),
    SBERBANK_REFUND_WITH_TPNET("SBERBANK_REFUND_WITH_TPNET"),
    PAYMENT_GIVEAWAY("give_away"),
    UNIFIED_PREPAYMENT("UNIFIED_PREPAYMENT"),
    POD_PAYMENT("POD_PAYMENT"),
    POD_AGENT("POD_AGENT"),
    TP_NET_INTERACTION("TP_NET_INTERACTION")
}

fun getProcessByType(type: String): Process {
    return Process.values().find{ it.processName == type } ?: throw IllegalArgumentException("Unknown process type : $type")
}