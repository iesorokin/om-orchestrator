package ru.iesorokin.orchestrator.config.bpmn

import org.camunda.bpm.engine.impl.history.HistoryLevel
import org.camunda.bpm.engine.impl.history.event.HistoryEventType
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes
import org.camunda.bpm.engine.runtime.Incident.EXTERNAL_TASK_HANDLER_TYPE
import org.camunda.bpm.engine.runtime.Incident.FAILED_JOB_HANDLER_TYPE
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration
import org.camunda.bpm.spring.boot.starter.configuration.Ordering
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordering.DEFAULT_ORDER + 1)
class ProcessEngineConfig : AbstractCamundaConfiguration() {
    override fun preInit(configuration: SpringProcessEngineConfiguration) {
        configuration.isBpmnStacktraceVerbose = false
        configuration.incidentHandlers = mapOf(
                FAILED_JOB_HANDLER_TYPE to IncidentHandlerWithLogging(FAILED_JOB_HANDLER_TYPE),
                EXTERNAL_TASK_HANDLER_TYPE to IncidentHandlerWithLogging(EXTERNAL_TASK_HANDLER_TYPE)
        )
        configuration.commandContextFactory = CommandContextFactory().also {
            it.processEngineConfiguration = configuration
        }

        configureHistoryLevel(configuration)
    }

    private fun configureHistoryLevel(configuration: SpringProcessEngineConfiguration) {
        var customHistoryLevels = configuration.customHistoryLevels

        customHistoryLevels?.let {
            customHistoryLevels = ArrayList()
            configuration.customHistoryLevels = customHistoryLevels
        }

        val orchestratorHistoryLevel = OrchestratorHistoryLevel()
        customHistoryLevels.add(orchestratorHistoryLevel)
        configuration.historyLevel = orchestratorHistoryLevel
    }

    private data class OrchestratorHistoryLevel(
            private val name: String = "orchestrator-level",
            private val id: Int = 100) : HistoryLevel {
        override fun getName() = name
        override fun getId() = id

        override fun isHistoryEventProduced(eventType: HistoryEventType, entity: Any?) =
                eventType == HistoryEventTypes.PROCESS_INSTANCE_START ||
                        eventType == HistoryEventTypes.INCIDENT_CREATE
    }
}