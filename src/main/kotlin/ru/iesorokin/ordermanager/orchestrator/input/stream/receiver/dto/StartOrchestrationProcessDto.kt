package ru.iesorokin.ordermanager.orchestrator.input.stream.receiver.dto

data class StartOrchestrationProcessDto(
        val businessProcessType: String,
        val businessKey: String,
        val variables: Map<String, Any>?
)