package ru.iesorokin.payment.orchestrator.core.service.giveaway

import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.ATOL_GIVE_AWAY_SUCCESS
import ru.iesorokin.payment.orchestrator.core.service.CamundaService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.AtolTransactionMessage

@Service
class AtolGiveAwayMessageService(
        private val camundaService: CamundaService
) {
    fun processMessage(message: AtolTransactionMessage) {
        when {
            message.correlationKey != null -> camundaService.createMessageCorrelation(
                    businessKey = message.correlationKey, processEvent = ATOL_GIVE_AWAY_SUCCESS
            )
            message.processId != null -> camundaService.createMessageCorrelation( //todo: to delete after 01.04.2020
                    businessKey = message.processId, processEvent = ATOL_GIVE_AWAY_SUCCESS
            )
            else -> processMessageByOldVersion(message) //todo: to delete after 01.04.2020
        }
    }

    private fun processMessageByOldVersion(message: AtolTransactionMessage) {
        val processInstance = camundaService.findProcessInstance(message.processInstanceId!!)
                ?: error("ProcessInstance not found for id: ${message.processInstanceId} for give away receive")

        camundaService.sendBusinessProcessMessage(
                processInstance.processInstanceId,
                ATOL_GIVE_AWAY_SUCCESS
        )
    }

}
