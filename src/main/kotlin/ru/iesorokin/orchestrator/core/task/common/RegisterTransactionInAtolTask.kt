package ru.iesorokin.orchestrator.core.task.common

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.ATOL_REGISTER_ID
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.service.AtolService
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.core.service.SolutionService
import ru.iesorokin.orchestrator.core.task.variable
import ru.iesorokin.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
class RegisterTransactionInAtolTask(
        private val solutionService: SolutionService,
        private val atolService: AtolService,
        private val paymentTaskService: PaymentTaskService
) : JavaDelegate {

    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) = try {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        val extOrderId = execution.variable(EXT_ORDER_ID)

        log.info { "Start registering transaction in atol with paymentTaskId: $paymentTaskId and extOrderId: $extOrderId" }
        val task = paymentTaskService.getPaymentTask(paymentTaskId)
        val solution = solutionService.getSolutionOrder(extOrderId)

        val atolId = atolService.registerAtolSale(task, solution, execution.businessKey)
        execution.setVariable(ATOL_REGISTER_ID, atolId)
        log.info { "Success registration in atol, atoldId: $atolId, paymentTaskId: $paymentTaskId, extOrderId: $extOrderId" }
    } catch (e: Exception) {
        log.error(e) { "Error on atol registration with businessKey: ${execution.businessKey}" }
        throw e
    }

}
