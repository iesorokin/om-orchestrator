package ru.iesorokin.orchestrator.input.stream.receiver

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.config.START_ORCHESTRATION_PROCESS
import ru.iesorokin.orchestrator.core.enums.bpmn.getProcessByType
import ru.iesorokin.orchestrator.core.service.CamundaService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.StartOrchestrationProcessDto

private val log = KotlinLogging.logger {}

@Service
class StartOrchestrationProcessMessageReceiver(
        val camundaService: CamundaService,
        @Value("\${orchestrator.consumer.maxRetry}")
        private val maxRetry: Long
) {

    @StreamListener(START_ORCHESTRATION_PROCESS)
    fun receiveApproveMessage(@Payload message: StartOrchestrationProcessDto,
                              @Header(name = X_DEATH_HEADER, required = false) death: Map<Any, Any?>?) {
        log.logInputMessage(START_ORCHESTRATION_PROCESS, message)
        if (death.isDeadLetterCountOverflown(maxRetry)) {
            log.deadLetterCountOverflownError(maxRetry, START_ORCHESTRATION_PROCESS, message)
            return
        }

        camundaService.startProcess(getProcessByType(message.businessProcessType), message.businessKey, message.variables)
    }
}
