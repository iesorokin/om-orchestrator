package ru.iesorokin.orchestrator.core.task.prepayment

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.service.TpNetService
import ru.iesorokin.orchestrator.core.task.variable
import ru.iesorokin.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Component
class PlaceTpNetDepositTask(private val tpNetService: TpNetService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        log.info { "Start tpNet deposit paymentTaskId: $paymentTaskId, processInstanceId=${execution.processInstanceId}" }
        tpNetService.doDeposit(paymentTaskId)
    }
}
