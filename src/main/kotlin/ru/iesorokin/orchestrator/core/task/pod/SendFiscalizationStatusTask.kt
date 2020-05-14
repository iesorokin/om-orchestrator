package ru.iesorokin.orchestrator.core.task.pod

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.FISCALIZATION_STATUS
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.FiscalizationStatus
import ru.iesorokin.orchestrator.core.service.SolutionService
import ru.iesorokin.orchestrator.core.task.variable
import ru.iesorokin.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class SendFiscalizationStatusTask(
        private val solutionService: SolutionService
) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        val extOrderId = execution.variable(EXT_ORDER_ID)
        val fiscalizationStatus = FiscalizationStatus.valueOf(execution.variable(FISCALIZATION_STATUS))
        log.info { "Send fiscalizationStatus $fiscalizationStatus for extOrderId: $extOrderId" }

        solutionService.sendFiscalizationStatus(paymentTaskId, extOrderId, fiscalizationStatus)
    }
}