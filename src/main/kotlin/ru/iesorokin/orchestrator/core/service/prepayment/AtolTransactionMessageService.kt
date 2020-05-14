package ru.iesorokin.orchestrator.core.service.prepayment

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_DOCUMENT_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_ID
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_REGISTRATION_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_STATUS
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_STORAGE_NUMBER
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_UUID
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent.ATOL_REGISTER_SUCCESS
import ru.iesorokin.orchestrator.core.service.CamundaService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.AtolTransactionMessage
import ru.iesorokin.orchestrator.sleuth.propagateOrchestrationData
import ru.iesorokin.utility.sleuthbase.MdcService

private val log = KotlinLogging.logger { }

@Service
class AtolTransactionMessageService(
        private val camundaService: CamundaService,
        private val mdcService: MdcService
) {
    fun processMessage(message: AtolTransactionMessage) {
        if (message.correlationKey != null) {
            val variablesToAddInContext: Map<String, Any> = mapOf(
                    ATOL_REGISTER_UUID to message.fiscalData.uuid,
                    ATOL_REGISTER_REGISTRATION_NUMBER to message.fiscalData.ecrRegistrationNumber,
                    ATOL_REGISTER_DOCUMENT_NUMBER to message.fiscalData.fiscalDocumentNumber,
                    ATOL_REGISTER_STORAGE_NUMBER to message.fiscalData.fiscalStorageNumber,
                    ATOL_REGISTER_STATUS to message.status
            )
            camundaService.createMessageCorrelation(
                    businessKey = message.correlationKey,
                    processEvent = ATOL_REGISTER_SUCCESS,
                    variables = variablesToAddInContext
            )
        } else {
            processMessageByOldVersion(message)
        }

    }

    private fun processMessageByOldVersion(message: AtolTransactionMessage) {
        val processInstance = try {
            camundaService.getPodPaymentProcessByVariable(ATOL_REGISTER_ID, message.atolId)
        } catch (ex: Throwable) {
            camundaService.getPrePaymentTpNetProcessByVariable(ATOL_REGISTER_ID, message.atolId)
        }

        log.info { "Processing message with atolId: ${message.atolId} for prepayment process: ${processInstance.processInstanceId}" }

        mdcService.propagateOrchestrationData(processInstance.processInstanceId, camundaService)

        val subscription = camundaService.getSubscriptionByProcessInstanceIdAndEventName(
                processInstance.processInstanceId,
                ATOL_REGISTER_SUCCESS.message
        )

        camundaService.executeEvent(
                subscription.eventName,
                subscription.executionId,
                mapOf(
                        ATOL_REGISTER_UUID to message.fiscalData.uuid,
                        ATOL_REGISTER_REGISTRATION_NUMBER to message.fiscalData.ecrRegistrationNumber,
                        ATOL_REGISTER_DOCUMENT_NUMBER to message.fiscalData.fiscalDocumentNumber,
                        ATOL_REGISTER_STORAGE_NUMBER to message.fiscalData.fiscalStorageNumber,
                        ATOL_REGISTER_STATUS to message.status
                )
        )
    }
}
