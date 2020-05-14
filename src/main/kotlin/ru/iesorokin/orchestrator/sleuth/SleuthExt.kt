package ru.iesorokin.orchestrator.sleuth

import mu.KotlinLogging
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.service.CamundaService
import ru.iesorokin.orchestrator.sleuth.SleuthEnum.PROCESS_ID
import ru.iesorokin.orchestrator.sleuth.SleuthEnum.SOLUTION_ID
import ru.iesorokin.utility.sleuthbase.MdcService

private val log = KotlinLogging.logger {}

internal fun MdcService.propagateOrchestrationData(processInstanceId: String, camundaService: CamundaService) =
        try {
            this.propagateMdc(PROCESS_ID.type, processInstanceId)
            val solutionId = camundaService.getVariable(processInstanceId, EXT_ORDER_ID) as String?
            solutionId?.let {
                this.propagateMdc(SOLUTION_ID.type, solutionId)
            }
        } catch (ex: Exception) {
            log.error { "Error propagation Data to MDC for processInstanceId: $processInstanceId" }
        }
