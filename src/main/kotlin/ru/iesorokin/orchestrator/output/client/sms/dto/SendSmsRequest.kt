package ru.iesorokin.orchestrator.output.client.sms.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.ALWAYS)
data class SendMultiSmsRequest(val event: String,
                               val receiver: String,
                               val sendingType: String,
                               val storeId: Int?,
                               val templateData: Map<String, String?>,
                               val templateOwner: String)