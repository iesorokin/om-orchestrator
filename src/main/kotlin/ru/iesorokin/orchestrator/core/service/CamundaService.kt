package ru.iesorokin.orchestrator.core.service

import mu.KotlinLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent
import ru.iesorokin.orchestrator.core.enums.bpmn.Process
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.POD_PAYMENT
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_PREPAYMENT_WITH_TPNET
import ru.iesorokin.orchestrator.core.enums.bpmn.Process.SBERBANK_REFUND_WITH_TPNET
import ru.iesorokin.orchestrator.core.exception.StartProcessException

private val log = KotlinLogging.logger { }

@Service
class CamundaService(private val runtimeService: RuntimeService) {

    fun startProcess(process: Process, businessKey: String, context: Map<String, Any>?): String {
        try {
            validateProcessAlreadyStarted(businessKey)
            val processInstance = runtimeService.startProcessInstanceByKey(process.processName, businessKey, context)
            log.info { "Process with businessKey: $businessKey was started. ProcessId: ${processInstance.processInstanceId}" }
            return processInstance.processInstanceId
        } catch (ex: Exception) {
            throw StartProcessException("Failed to start process $process, businessKey: $businessKey, context: $context", ex)
        }
    }

    fun startProcess(process: Process, context: Map<String, Any>): String {
        try {
            val processInstance = runtimeService.startProcessInstanceByKey(process.processName, context)
            log.info { "Process was started. ProcessId: ${processInstance.processInstanceId}" }
            return processInstance.processInstanceId
        } catch (ex: Exception) {
            throw StartProcessException("Failed to start process $process, context: $context", ex)
        }
    }

    private fun validateProcessAlreadyStarted(businessKey: String) {
        val processByBusinessKey = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .active()
                .list()
        if (processByBusinessKey.isNotEmpty()) {
            throw java.lang.IllegalStateException("Process instance with businessKey: $businessKey already started")
        }
    }

    @Deprecated(message = "Use @createMessageCorrelation instead, delete after 01.02.2020")
    fun sendBusinessProcessMessage(processInstanceId: String, eventName: BusinessProcessEvent) {
        val eventSubscription = getSubscriptionByProcessInstanceIdAndEventName(processInstanceId, eventName.message)
        runtimeService.messageEventReceived(eventSubscription.eventName, eventSubscription.executionId)
        log.info {
            "Message event has successfully received . ProcessInstance $processInstanceId, messageCode ${eventName.message}"
        }
    }

    /**
     * Try to correlate message event
     *
     * @throws MismatchingMessageCorrelationException not found waiting process
     */
    fun createMessageCorrelation(businessKey: String, processEvent: BusinessProcessEvent, variables: Map<String, Any>? = null) {
        runtimeService.createMessageCorrelation(processEvent.message)
                .processInstanceBusinessKey(businessKey)
                .setVariables(variables)
                .correlateWithResult()

        log.info {
            "Message event: $processEvent has successfully received for businessKey: $businessKey"
        }
    }

    fun getRefundTpNetProcessByUniqueVariableForProcess(variableName: String,
                                                        value: Any): ProcessInstance {
        try {
            return runtimeService
                    .createProcessInstanceQuery()
                    .active()
                    .variableValueEquals(variableName, value)
                    .processDefinitionKey(SBERBANK_REFUND_WITH_TPNET.processName)
                    .singleResult()!!
        } catch (e: ProcessEngineException) {
            log.error(e) { "Process with variableName=$variableName and value=$value is not single: ${e.message}" }
            throw e
        } catch (e: Throwable) {
            log.error(e) {
                "Something went wrong trying to get processInstance with variableName=$variableName " +
                        "and value=$value due to: ${e.message}"
            }
            throw e
        }
    }

    fun getPrePaymentTpNetProcessByVariable(variableName: String,
                                            value: Any): ProcessInstance =
            getActiveInstanceByVariableValue(variableName, value, SBERBANK_PREPAYMENT_WITH_TPNET)

    fun getPodPaymentProcessByVariable(variableName: String,
                                       value: Any): ProcessInstance =
            getActiveInstanceByVariableValue(variableName, value, POD_PAYMENT)

    private fun getActiveInstanceByVariableValue(variableName: String,
                                                 value: Any,
                                                 process: Process): ProcessInstance =
            try {
                runtimeService.createProcessInstanceQuery()
                        .processDefinitionKey(process.processName)
                        .variableValueEquals(variableName, value)
                        .active()
                        .singleResult()!!
            } catch (e: ProcessEngineException) {
                log.error(e) { "Process with variableName=$variableName and value=$value is not single: ${e.message}" }
                throw e
            } catch (e: Throwable) {
                log.warn(e) {
                    "Something went wrong trying to get processInstance with variableName=$variableName " +
                            "and value=$value due to: ${e.message}"
                }
                throw e
            }

    fun getSubscriptionByProcessInstanceIdAndEventName(processInstanceId: String,
                                                       eventName: String) =
            try {
                runtimeService.createEventSubscriptionQuery()
                        .processInstanceId(processInstanceId)
                        .eventName(eventName)
                        .singleResult()!!
            } catch (e: ProcessEngineException) {
                log.error(e) {
                    "Process with processInstanceId=$processInstanceId and eventName=$eventName is not single: ${e.message}"
                }
                throw e
            } catch (e: Throwable) {
                log.error(e) {
                    "Something went wrong trying to get eventSubscription with processInstanceId=$processInstanceId " +
                            "and eventName=$eventName due to: ${e.message}"
                }
                throw e
            }

    fun executeEvent(eventName: String,
                     executionId: String,
                     variables: Map<String, Any>? = null) =
            try {
                runtimeService.messageEventReceived(eventName, executionId, variables)
            } catch (e: ProcessEngineException) {
                log.error(e) {
                    "No such execution with executionId=$executionId or it has not subscribed to event=$eventName: ${e.message}"
                }
                throw e
            } catch (e: Throwable) {
                log.error {
                    "Something went wrong trying to execute event with executionId=$executionId " +
                            "and event=$eventName due to: ${e.message}"
                }
                throw e
            }

    fun getVariable(processInstanceId: String, variableName: String): Any? =
            runtimeService.getVariable(processInstanceId, variableName)

    @Deprecated("Use startProcess() instead")
    fun startProcessInstanceByKey(processInstanceId: String, context: Map<String, Any>): ProcessInstance =
            runtimeService.startProcessInstanceByKey(processInstanceId, context)

    fun findProcessInstanceByVariable(variableName: String, variableData: String) =
            runtimeService.createProcessInstanceQuery()
                    .active()
                    .variableValueEquals(variableName, variableData)
                    .singleResult()

    fun findProcessInstance(processInstanceId: String): ProcessInstance? =
            runtimeService.createProcessInstanceQuery()
                    .active()
                    .processInstanceId(processInstanceId)
                    .singleResult()

    fun updateVariableByProcessInstanceId(processInstanceId: String, variableName: String, variable: Any) {
        runtimeService.setVariable(processInstanceId, variableName, variable)
    }

    /**
     * @param process type of bpmn process
     * @return all active process instance by process instance id for current process name
     */
    fun getActiveProcessInstanceById(process: Process): Map<String, ProcessInstance> =
            getActiveProcessInstances(process)
                    .associateBy { it.processInstanceId }

    /**
     * @param process type of bpmn process
     * @return all active process instances for current process name
     */
    fun getActiveProcessInstances(process: Process): List<ProcessInstance> =
            runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey(process.processName)
                    .active()
                    .list()

    /**
     * @param process type of bpmn process
     * @return all active process instance by process instance id for current process name
     */
    fun getActiveProcessInstanceByBusinessKey(process: Process): Map<String, ProcessInstance> =
            runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey(process.processName)
                    .active()
                    .list()
                    .associateBy { it.businessKey }

    fun getProcessInstanceByBusinessKeyAndProcess(businessKey: String, process: Process): ProcessInstance =
            runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey(process.processName)
                    .processInstanceBusinessKey(businessKey)
                    .active()
                    .singleResult()!!

    fun getProcessInstanceByBusinessKey(businessKey: String): ProcessInstance =
            runtimeService.createProcessInstanceQuery()
                    .processInstanceBusinessKey(businessKey)
                    .active()
                    .singleResult()!!
}
