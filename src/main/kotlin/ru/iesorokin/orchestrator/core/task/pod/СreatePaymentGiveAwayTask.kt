package ru.iesorokin.orchestrator.core.task.pod

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.EXECUTION_STORE
import ru.iesorokin.orchestrator.core.constants.process.GIVE_AWAY_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.core.task.variable
import ru.iesorokin.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class CreatePaymentGiveAwayTask(
        private val paymentTaskService: PaymentTaskService
) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        log.info { "Create give-away object for $paymentTaskId and workflow id ${execution.processInstanceId}" }

        val paymentTask = paymentTaskService.getPaymentTask(paymentTaskId)
        val giveAwayId = paymentTaskService.createGiveAway(paymentTask)

        execution.setVariable(GIVE_AWAY_ID, giveAwayId)
        execution.setVariable(EXECUTION_STORE, paymentTask.executionStore)
    }
}