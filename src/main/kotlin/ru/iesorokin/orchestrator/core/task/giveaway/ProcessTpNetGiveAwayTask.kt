package ru.iesorokin.payment.orchestrator.core.task.giveaway

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.service.TpNetService
import ru.iesorokin.payment.orchestrator.core.task.variable

private val log = KotlinLogging.logger {}

@Service
class ProcessTpNetGiveAwayTask(private val tpNetService: TpNetService) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        log.info { "Starting give away task in process: ${execution.processInstanceId}" }
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        tpNetService.doGiveAway(paymentTaskId)
    }
}