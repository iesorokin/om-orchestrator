package ru.iesorokin.orchestrator.output.client.dto

import com.fasterxml.jackson.annotation.JsonValue

data class JsonPatchRequestOperation(val op: OperationType,
                                     val path: String,
                                     val value: Any)

enum class OperationType(@field:JsonValue val value: String) {
    REPLACE("replace"),
    ADD("add")
}
