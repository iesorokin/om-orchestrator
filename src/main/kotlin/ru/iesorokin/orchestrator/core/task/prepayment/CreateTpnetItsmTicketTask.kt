package ru.iesorokin.orchestrator.core.task.prepayment

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.constants.process.TP_NET_OPERATION_TYPE
import ru.iesorokin.orchestrator.core.domain.TpnetItsmTicket
import ru.iesorokin.orchestrator.core.service.ItsmService
import ru.iesorokin.orchestrator.core.task.variable
import ru.iesorokin.orchestrator.core.task.variableLocal
import ru.iesorokin.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Component
class CreateTpnetItsmTicketTask(private val itsmService: ItsmService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        val processInstanceId = execution.processInstanceId
        val tpnetOperationType = execution.variableLocal(TP_NET_OPERATION_TYPE)

        log.info { "Create TpNet ITSM ticket with paymentTaskId: $paymentTaskId, " +
                "processInstanceId:  $processInstanceId and tpnetOperationType $tpnetOperationType" }
        val tpnetItsmTicket = TpnetItsmTicket(
                paymentTaskId = paymentTaskId,
                processInstanceId = processInstanceId,
                tpnetOperationType = tpnetOperationType
        )
        itsmService.createTicket(tpnetItsmTicket)
    }
}
