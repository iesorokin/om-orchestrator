package ru.iesorokin.payment.orchestrator.core.task.refund

import mu.KotlinLogging
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.payment.orchestrator.core.service.SmsService
import ru.iesorokin.payment.orchestrator.core.task.variable

private val log = KotlinLogging.logger { }

@Service
class SendRefundSmsTask(private val smsService: SmsService) : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        val paymentTaskId = execution.variable(PAYMENT_TASK_ID)
        val extOrderId = execution.variable(EXT_ORDER_ID)
        val workflowId = execution.businessKey
        try {
            log.info { "Sending refund sms paymentTaskId: $paymentTaskId extOrderId: $extOrderId workflowId: $workflowId" }
            smsService.sendMultiSms(paymentTaskId, workflowId, extOrderId)
        } catch (e: Exception) {
            log.error(e) { "Error sending refund sms paymentTaskId: $paymentTaskId extOrderId: $extOrderId workflowId: $workflowId" }
        }
    }
}
