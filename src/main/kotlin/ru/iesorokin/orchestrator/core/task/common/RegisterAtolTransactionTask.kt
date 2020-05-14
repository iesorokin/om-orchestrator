package ru.iesorokin.payment.orchestrator.core.task.common

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.constants.process.ATOL_REGISTER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskRegisterStatusLine
import ru.iesorokin.payment.orchestrator.core.enums.TaskType
import ru.iesorokin.payment.orchestrator.core.service.AtolService
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.payment.orchestrator.core.service.SolutionService
import ru.iesorokin.payment.orchestrator.core.task.variable
import ru.iesorokin.payment.orchestrator.sleuth.ExtractProcessDataFromDelegate

private val log = KotlinLogging.logger {}

@Service
@Deprecated("Should delete after 01.05.2020, useless class")
class RegisterAtolTransactionTask(private val solutionService: SolutionService,
                                  private val atolService: AtolService,
                                  private val paymentTaskService: PaymentTaskService) : JavaDelegate {
    @ExtractProcessDataFromDelegate
    override fun execute(execution: DelegateExecution) = try {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        val extOrderId = execution.variable(EXT_ORDER_ID)
        val task = paymentTaskService.getPaymentTask(paymentTaskId)
        val solution = solutionService.getSolutionOrder(extOrderId)

        val atolId = atolService.registerAtolSale(task, solution, execution.businessKey)
        execution.setVariable(ATOL_REGISTER_ID, atolId)
        paymentTaskService.updateRegisterStatus(paymentTaskId, PaymentTaskRegisterStatus(
                lines = task.lines.map {
                    val quantity = if (task.taskType == TaskType.POD_POST_PAYMENT.name) it.confirmedQuantity else it.quantity
                    PaymentTaskRegisterStatusLine(it.extLineId!!, quantity, it.unitAmountIncludingVat)
                }
        ))
    } catch (e: Exception) {
        log.error(e) { "Error on atol registration with execution: $execution" }
        throw e
    }
}
