package ru.iesorokin.orchestrator.core.service

import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent.BILLING_PAID_STATUS
import ru.iesorokin.orchestrator.input.stream.receiver.dto.CorrelationMessage

@Service
class BillingService(private val camundaService: CamundaService) {

    fun processBillingPaidStatusMessage(message: CorrelationMessage) {
        camundaService.createMessageCorrelation(
                message.correlationKey,
                BILLING_PAID_STATUS
        )
    }

}
