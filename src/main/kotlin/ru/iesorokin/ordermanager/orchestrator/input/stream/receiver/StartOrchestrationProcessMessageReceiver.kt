package ru.iesorokin.ordermanager.orchestrator.input.stream.receiver

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.ordermanager.orchestrator.config.START_ORCHESTRATION_PROCESS
import ru.iesorokin.ordermanager.orchestrator.core.enums.bpmn.getProcessByType
import ru.iesorokin.ordermanager.orchestrator.core.service.CamundaService
import ru.iesorokin.ordermanager.orchestrator.core.service.StreamLoggerService
import ru.iesorokin.ordermanager.orchestrator.input.stream.receiver.dto.StartOrchestrationProcessDto

@Service
class StartOrchestrationProcessMessageReceiver(
        val camundaService: CamundaService,
        @Value("\${orchestrator.consumer.maxRetry}")
        private val maxRetry: Long,
        private val streamLogger: StreamLoggerService
) {

    @StreamListener(START_ORCHESTRATION_PROCESS)
    fun receiveApproveMessage(@Payload message: StartOrchestrationProcessDto,
                              @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        streamLogger.inputMessage(START_ORCHESTRATION_PROCESS, message)
        if (death.isDeadLetterCountOverflown(maxRetry)) {
            streamLogger.deadLetterCountOverflownError(maxRetry, START_ORCHESTRATION_PROCESS, message)
            return
        }

        camundaService.startProcess(getProcessByType(message.businessProcessType), message.businessKey, message.variables)
    }
}
