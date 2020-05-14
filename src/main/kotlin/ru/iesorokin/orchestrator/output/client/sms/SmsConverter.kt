package ru.iesorokin.orchestrator.output.client.sms

import org.springframework.stereotype.Component
import ru.iesorokin.orchestrator.core.domain.InternalSmsRequest
import ru.iesorokin.orchestrator.output.client.sms.dto.SendMultiSmsRequest

@Component
class SmsConverter {

    private fun internalSmsRequestToSendMultiSmsRequest(internalSmsRequest: InternalSmsRequest): SendMultiSmsRequest {
        return SendMultiSmsRequest(
                internalSmsRequest.event,
                internalSmsRequest.receiver,
                internalSmsRequest.sendingType,
                internalSmsRequest.storeId,
                internalSmsRequest.templateData,
                internalSmsRequest.templateOwner
        )
    }

    fun smsRequestListConvert(internalList: List<InternalSmsRequest>): List<SendMultiSmsRequest> {
        return internalList.map { internalSmsRequestToSendMultiSmsRequest(it)}.toList()
    }
}
