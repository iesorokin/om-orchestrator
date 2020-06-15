package ru.iesorokin.ordermanager.orchestrator.core.task.refund

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.ordermanager.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.ordermanager.orchestrator.core.enums.bpmn.PaymentTransactionStatus
import ru.iesorokin.ordermanager.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.ordermanager.orchestrator.core.service.ValidationService
import ru.iesorokin.ordermanager.orchestrator.core.task.variable

private val log = KotlinLogging.logger {}

@Service
class StartHold(
        val paymentTaskService: PaymentTaskService,
        val validationService: ValidationService
) : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        log.info { "Start hold Service" }

        throw RuntimeException()



    }
}
