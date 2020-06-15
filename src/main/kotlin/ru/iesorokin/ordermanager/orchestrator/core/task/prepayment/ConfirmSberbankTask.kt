package ru.iesorokin.ordermanager.orchestrator.core.task.prepayment

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import ru.iesorokin.ordermanager.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.ordermanager.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.ordermanager.orchestrator.core.service.prepayment.SberbankDepositService
import ru.iesorokin.ordermanager.orchestrator.core.task.variable
import ru.iesorokin.ordermanager.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Component
class ConfirmSberbankTask(private val sberbankDepositService: SberbankDepositService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val taskId = execution.variable(PAYMENT_TASK_ID)
        log.info { "Start sberbank deposit taskId: $taskId, processInstanceId=${execution.processInstanceId}" }

        sberbankDepositService.doDeposit(
                taskId = taskId,
                orderId = execution.variable(EXT_ORDER_ID),
                correlationKey = execution.businessKey
        )
    }
}
