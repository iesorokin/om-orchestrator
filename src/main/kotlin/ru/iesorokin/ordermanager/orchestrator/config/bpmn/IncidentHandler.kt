package ru.iesorokin.ordermanager.orchestrator.config.bpmn

import mu.KotlinLogging
import org.camunda.bpm.engine.impl.incident.DefaultIncidentHandler
import org.camunda.bpm.engine.impl.incident.IncidentContext
import org.camunda.bpm.engine.impl.incident.IncidentHandler
import org.camunda.bpm.engine.runtime.Incident
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

class IncidentHandlerWithLogging(type: String) : IncidentHandler {
    private val defaultHandler: IncidentHandler

    init {
        this.defaultHandler = DefaultIncidentHandler(type)
    }

    override fun handleIncident(context: IncidentContext, message: String?): Incident {
        log.error {
            """New incident was created.
                |processInstanceId: ${context.executionId}
                |activityId: ${context.activityId}
                |time: ${LocalDateTime.now()}
                |cause: $message
            |""".trimMargin()
        }

        return defaultHandler.handleIncident(context, message)
    }

    override fun getIncidentHandlerType(): String = defaultHandler.incidentHandlerType

    override fun resolveIncident(context: IncidentContext) = defaultHandler.resolveIncident(context)

    override fun deleteIncident(context: IncidentContext) = defaultHandler.deleteIncident(context)
}
