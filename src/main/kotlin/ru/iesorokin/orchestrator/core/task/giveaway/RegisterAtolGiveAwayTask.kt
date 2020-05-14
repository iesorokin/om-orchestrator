package ru.iesorokin.orchestrator.core.task.giveaway

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.ATOL_GIVE_AWAY_ID
import ru.iesorokin.orchestrator.core.constants.process.EXECUTION_STORE
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.service.AtolService
import ru.iesorokin.orchestrator.core.task.intVariable
import ru.iesorokin.orchestrator.core.task.variable
import ru.iesorokin.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class RegisterAtolGiveAwayTask(private val atolService: AtolService) : JavaDelegate {

    @ExtractProcessDataFromDelegate
    //@TODO remove processInstanceId usage in registerAtolGiveAway and AtolGiveAwayRequest after 01.04.2020 (it makes for release backward compatibility)
    override fun execute(execution: DelegateExecution) {
        val solutionId = execution.variable(EXT_ORDER_ID)
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        val storeId = execution.intVariable(EXECUTION_STORE)
        val businessKey = execution.businessKey
        log.info { "Register give away in atol with payment task $paymentTaskId and workflow id ${execution.processInstanceId}" }
        val atolGiveAwayId = atolService.registerAtolGiveAway(
                solutionId = solutionId,
                paymentTaskId = paymentTaskId,
                giveAwayId = businessKey,
                correlationKey = businessKey,
                processInstanceId = execution.processInstanceId,
                storeId = storeId
        )
        execution.setVariable(ATOL_GIVE_AWAY_ID, atolGiveAwayId)
    }
}