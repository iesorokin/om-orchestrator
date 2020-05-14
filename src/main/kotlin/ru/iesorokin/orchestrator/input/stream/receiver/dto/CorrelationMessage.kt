package ru.iesorokin.orchestrator.input.stream.receiver.dto

data class CorrelationMessage(
        val sender: String,
        val correlationKey: String,
        val variables: Map<String, Any>? = null
)