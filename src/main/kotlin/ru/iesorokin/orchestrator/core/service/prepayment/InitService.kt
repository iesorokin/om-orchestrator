package ru.iesorokin.orchestrator.core.service.prepayment

import mu.KotlinLogging
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.exception.NullValueException
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.service.CamundaService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.StartProcessMessage
import ru.iesorokin.orchestrator.sleuth.propagateOrchestrationData
import ru.iesorokin.utility.sleuthbase.MdcService

private val log = KotlinLogging.logger {}

@Service
@Deprecated("Use CamundaService.startProcess() instead")
class InitService(
        private val camundaService: CamundaService,
        private val mdcService: MdcService
) {

    fun initPrepaymentProcess(startProcessMessage: StartProcessMessage) {
        val context = mapOf(
                PAYMENT_TASK_ID to startProcessMessage.paymentTaskId as Any,
                EXT_ORDER_ID to startProcessMessage.extOrderId as Any)
        initProcess(
                processDefinitionKey = startProcessMessage.workflowType,
                context = context,
                verifierContextKey = PAYMENT_TASK_ID
        )
    }

    fun initProcess(processDefinitionKey: String, context: Map<String, Any>, verifierContextKey: String? = null) {
        try {
            if (!validateContextKey(verifierContextKey, context)) {
                log.error { "I haven't created duplicate process with processDefinitionKey=$processDefinitionKey." }
                return
            }
            val processInstance = camundaService.startProcessInstanceByKey(processDefinitionKey, context)

            mdcService.propagateOrchestrationData(processInstance.processInstanceId, camundaService)
            log.info {
                "ProcessInstance with processInstanceId=${processInstance.processInstanceId}, " +
                        "processDefinitionKey=$processDefinitionKey has successfully started"
            }
        } catch (e: NullValueException) {
            log.error(e) {
                "No process deployed with processDefinitionKey=$processDefinitionKey."
            }
            throw e
        } catch (e: ProcessEngineException) {
            log.error(e) {
                "Two running processes have been detected in the context with processDefinitionKey=$processDefinitionKey."
            }
            throw e
        }
    }

    private fun validateContextKey(verifierContextKey: String?, context: Map<String, Any>): Boolean {
        if (verifierContextKey != null && context[verifierContextKey] != null) {
            return camundaService.findProcessInstanceByVariable(verifierContextKey, context[verifierContextKey].toString()) == null
        }
        return true
    }

}
