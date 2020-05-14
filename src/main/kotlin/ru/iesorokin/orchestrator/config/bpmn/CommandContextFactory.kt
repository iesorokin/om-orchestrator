package ru.iesorokin.payment.orchestrator.config.bpmn

import mu.KotlinLogging
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.TransactionContextFactory
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.interceptor.CommandContextFactory
import org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext

private val log = KotlinLogging.logger {}

class CommandContextFactory : CommandContextFactory() {
    override fun createCommandContext(): CommandContext = ContextCommandWithoutLmExceptionsLogging(processEngineConfiguration)
}

private class ContextCommandWithoutLmExceptionsLogging : CommandContext {
    constructor(processEngineConfiguration: ProcessEngineConfigurationImpl) : super(processEngineConfiguration)
    constructor(processEngineConfiguration: ProcessEngineConfigurationImpl, transactionContextFactory: TransactionContextFactory) : super(processEngineConfiguration, transactionContextFactory)

    override fun close(commandInvocationContext: CommandInvocationContext) {
        if (commandInvocationContext.throwable != null && isLmException(commandInvocationContext.throwable)) {
            try {
                log.warn(commandInvocationContext.throwable) { "Exception while closing command context" }
                fireCommandFailed(commandInvocationContext.throwable)
                transactionContext.rollback()
                commandInvocationContext.rethrow()
            } catch (exception: Throwable) {
                commandInvocationContext.trySetThrowable(exception)
            } finally {
                closeSessions(commandInvocationContext)
            }
        } else {
            super.close(commandInvocationContext)
        }
    }

    private fun isLmException(exception: Throwable) =
            exception::class.qualifiedName?.contains("ru.iesorokin") ?: false

}