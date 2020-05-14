package ru.iesorokin.payment.orchestrator.core.service.prepayment

import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskLine
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.DepositEventType
import ru.iesorokin.payment.orchestrator.core.service.CamundaService
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.payment.orchestrator.output.stream.sender.SberbankDepositCommandMessageSender
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class DepositService(private val sberbankDepositCommandSender: SberbankDepositCommandMessageSender,
                     private val paymentTaskService: PaymentTaskService,
                     private val camundaService: CamundaService) {

    fun doDeposit(taskId: String, orderId: String) {
        val paymentTask = paymentTaskService.getPaymentTask(taskId)
        if (paymentTask.executionStore == null) {
            throw IllegalStateException("Payment task $paymentTask has null executionStore")
        }

        sberbankDepositCommandSender.sendDepositComand(orderId, calculateDepositAmount(paymentTask), paymentTask.executionStore)
    }

    fun handleDepositEvent(orderId: String, depositEventType: DepositEventType) {
        val processInstance = camundaService.getPrePaymentTpNetProcessByVariable(EXT_ORDER_ID, orderId)
        val subscription = camundaService.getSubscriptionByProcessInstanceIdAndEventName(
                processInstanceId = processInstance.processInstanceId,
                eventName = when(depositEventType) {
                    DepositEventType.SUCCESS -> BusinessProcessEvent.SBERBANK_DEPOSIT_SUCCESS.message
                    DepositEventType.FAIL -> BusinessProcessEvent.SBERBANK_DEPOSIT_FAIL.message
                }
        )

        camundaService.executeEvent(subscription.eventName, subscription.executionId)
    }

    private fun calculateDepositAmount(paymentTask: PaymentTask) =
            paymentTask.lines
                    .fold(BigDecimal.ZERO) { acc, line ->  acc.add(line.depositAmount())}
}

private fun PaymentTaskLine.depositAmount() = confirmedQuantity.multiply(unitAmountIncludingVat)
        .setScale(2, RoundingMode.HALF_UP)

