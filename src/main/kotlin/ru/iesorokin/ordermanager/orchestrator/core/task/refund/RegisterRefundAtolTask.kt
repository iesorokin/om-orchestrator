package ru.iesorokin.ordermanager.orchestrator.core.task.refund

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.ordermanager.orchestrator.core.constants.process.ATOL_REFUND_ID
import ru.iesorokin.ordermanager.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.ordermanager.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.ordermanager.orchestrator.core.service.AtolService
import ru.iesorokin.ordermanager.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.ordermanager.orchestrator.core.service.SolutionService
import ru.iesorokin.ordermanager.orchestrator.core.task.variable
import ru.iesorokin.ordermanager.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class RegisterRefundAtolTask(private val solutionService: SolutionService,
                             private val atolService: AtolService,
                             private val paymentTaskService: PaymentTaskService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) = try {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        val extOrderId = execution.variable(EXT_ORDER_ID)
        val task = paymentTaskService.getPaymentTask(paymentTaskId)
        val solution = solutionService.getSolutionOrder(extOrderId)
        val workflowId = execution.businessKey

        val atolId = atolService.registerAtolRefund(
                task, solution, workflowId, execution.businessKey
        )
        log.info { "Update refund process with workflowId: $workflowId by atolRefundId: $atolId" }
        execution.setVariable(ATOL_REFUND_ID, atolId)
    } catch (e: Exception) {
        log.error(e) { "Error on atol refund with execution: $execution" }
        throw e
    }
}
