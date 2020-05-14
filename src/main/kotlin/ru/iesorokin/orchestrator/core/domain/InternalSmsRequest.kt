package ru.iesorokin.payment.orchestrator.core.domain

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.ALWAYS)
data class InternalSmsRequest(val event: String,
                              val receiver: String,
                              val sendingType: String,
                              val storeId: Int?,
                              val templateData: Map<String, String?>,
                              val templateOwner: String)
