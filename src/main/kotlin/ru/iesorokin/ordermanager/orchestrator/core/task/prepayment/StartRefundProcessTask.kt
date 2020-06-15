package ru.iesorokin.ordermanager.orchestrator.core.task.prepayment

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.ordermanager.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.ordermanager.orchestrator.core.service.refund.RefundService
import ru.iesorokin.ordermanager.orchestrator.core.task.variable
import ru.iesorokin.ordermanager.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class StartRefundProcessTask(private val refundService: RefundService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        log.info { "Start refund process for paymentTaskId: $paymentTaskId" }
        refundService.startRefundProcess(paymentTaskId)
    }
}
