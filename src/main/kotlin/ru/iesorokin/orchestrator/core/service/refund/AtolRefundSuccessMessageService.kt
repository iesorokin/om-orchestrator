package ru.iesorokin.payment.orchestrator.core.service.refund

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REFUND_DOCUMENT_NUMBER
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REFUND_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REFUND_REGISTRATION_NUMBER
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REFUND_STORAGE_NUMBER
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REFUND_UUID
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.ATOL_REFUND_SUCCESS
import ru.iesorokin.payment.orchestrator.core.service.CamundaService
import ru.iesorokin.payment.orchestrator.input.stream.receiver.dto.AtolTransactionMessage
import ru.iesorokin.payment.orchestrator.sleuth.propagateOrchestrationData
import ru.iesorokin.utility.sleuthbase.MdcService

private val log = KotlinLogging.logger { }

@Service
class AtolRefundSuccessMessageService(
        private val camundaService: CamundaService,
        private val mdcService: MdcService
) {
    fun processMessage(message: AtolTransactionMessage) {
        if (message.correlationKey != null) {
            val variablesToAddInContext: Map<String, Any> = mapOf(
                    ATOL_REFUND_UUID to message.fiscalData.uuid,
                    ATOL_REFUND_REGISTRATION_NUMBER to message.fiscalData.ecrRegistrationNumber,
                    ATOL_REFUND_DOCUMENT_NUMBER to message.fiscalData.fiscalDocumentNumber,
                    ATOL_REFUND_STORAGE_NUMBER to message.fiscalData.fiscalStorageNumber
            )
            camundaService.createMessageCorrelation(
                    businessKey = message.correlationKey,
                    processEvent = ATOL_REFUND_SUCCESS,
                    variables = variablesToAddInContext
            )
        } else {
            processMessageByOldVersion(message)
        }

    }

    private fun processMessageByOldVersion(message: AtolTransactionMessage) {
        val processInstance = camundaService.getRefundTpNetProcessByUniqueVariableForProcess(
                ATOL_REFUND_ID,
                message.atolId
        )
        log.info { "Processing message with atolId: ${message.atolId} for refund process: ${processInstance.processInstanceId}" }

        mdcService.propagateOrchestrationData(processInstance.processInstanceId, camundaService)

        val subscription = camundaService.getSubscriptionByProcessInstanceIdAndEventName(
                processInstance.processInstanceId,
                ATOL_REFUND_SUCCESS.message
        )

        camundaService.executeEvent(
                subscription.eventName,
                subscription.executionId,
                mapOf(
                        ATOL_REFUND_UUID to message.fiscalData.uuid,
                        ATOL_REFUND_REGISTRATION_NUMBER to message.fiscalData.ecrRegistrationNumber,
                        ATOL_REFUND_DOCUMENT_NUMBER to message.fiscalData.fiscalDocumentNumber,
                        ATOL_REFUND_STORAGE_NUMBER to message.fiscalData.fiscalStorageNumber
                )
        )
    }
}
