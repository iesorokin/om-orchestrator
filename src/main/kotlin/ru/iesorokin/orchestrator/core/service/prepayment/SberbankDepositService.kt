package ru.iesorokin.orchestrator.core.service.prepayment

import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.EXT_ORDER_ID
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.domain.PaymentTaskLine
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent
import ru.iesorokin.orchestrator.core.enums.bpmn.DepositEventType
import ru.iesorokin.orchestrator.core.service.CamundaService
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.input.stream.receiver.dto.SberbankDepositEventMessage
import ru.iesorokin.orchestrator.output.stream.sender.SberbankDepositCommandMessageSender
import ru.iesorokin.orchestrator.sleuth.propagateOrchestrationData
import ru.iesorokin.utility.sleuthbase.MdcService
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class SberbankDepositService(private val sberbankDepositCommandSender: SberbankDepositCommandMessageSender,
                             private val paymentTaskService: PaymentTaskService,
                             private val camundaService: CamundaService,
                             private val mdcService: MdcService) {

    fun doDeposit(taskId: String, orderId: String, correlationKey: String? = null) {
        val paymentTask = paymentTaskService.getPaymentTask(taskId)
        if (paymentTask.executionStore == null) {
            throw IllegalStateException("payment task $paymentTask has null executionStore")
        }

        sberbankDepositCommandSender.sendDepositComand(orderId, calculateDepositAmount(paymentTask), paymentTask.executionStore, correlationKey)
    }

    fun handleDepositEvent(message: SberbankDepositEventMessage, depositEventType: DepositEventType) {
        val processInstance = getProcessInstance(message)

        mdcService.propagateOrchestrationData(processInstance.processInstanceId, camundaService)

        correlateMessage(processInstance, depositEventType)
    }

    private fun correlateMessage(processInstance: ProcessInstance, depositEventType: DepositEventType) {
        val event = when (depositEventType) {
            DepositEventType.SUCCESS -> BusinessProcessEvent.SBERBANK_DEPOSIT_SUCCESS
            DepositEventType.FAIL -> BusinessProcessEvent.SBERBANK_DEPOSIT_FAIL
        }

        if (processInstance.businessKey != null) {
            camundaService.createMessageCorrelation(processInstance.businessKey, event)
        } else {
            //@TODO remove else block when SBERBANK_PREPAYMENT_WITH_TPNET will start by businessKey and all processInstances without businessKey will be completed
            val subscription = camundaService.getSubscriptionByProcessInstanceIdAndEventName(
                    processInstanceId = processInstance.processInstanceId,
                    eventName = event.message
            )

            camundaService.executeEvent(subscription.eventName, subscription.executionId)
        }

    }

    private fun getProcessInstance(message: SberbankDepositEventMessage) =
        if (message.correlationKey != null) {
            camundaService.getProcessInstanceByBusinessKey(message.correlationKey)
        } else {
            //@TODO remove else block when SBERBANK_PREPAYMENT_WITH_TPNET will start by businessKey and all processInstances without businessKey will be completed
            camundaService.getPrePaymentTpNetProcessByVariable(EXT_ORDER_ID, message.orderId)
        }



    private fun calculateDepositAmount(paymentTask: PaymentTask) =
            paymentTask.lines
                    .fold(BigDecimal.ZERO) { acc, line -> acc + line.depositAmount() }
}

private fun PaymentTaskLine.depositAmount() = confirmedQuantity.multiply(unitAmountIncludingVat)
        .setScale(2, RoundingMode.HALF_UP)

