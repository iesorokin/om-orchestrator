package ru.iesorokin.orchestrator.core.service

import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.constants.process.PAYMENT_TASK_ID
import ru.iesorokin.orchestrator.core.enums.bpmn.BusinessProcessEvent
import ru.iesorokin.orchestrator.core.enums.bpmn.DepositEventType
import ru.iesorokin.orchestrator.output.stream.sender.TpNetSender
import ru.iesorokin.orchestrator.sleuth.propagateOrchestrationData
import ru.iesorokin.utility.sleuthbase.MdcService

@Service
class TpNetService(private val tpNetSender: TpNetSender,
                   private val camundaService: CamundaService,
                   private val mdcService: MdcService) {

    fun doDeposit(paymentTaskId: String) {
        tpNetSender.sendTpNetDepositCommandMessage(paymentTaskId)
    }

    fun handleDepositEvent(paymentTaskId: String, depositEventType: DepositEventType) {
        val event = when (depositEventType) {
            DepositEventType.SUCCESS -> BusinessProcessEvent.TPNET_DEPOSIT_SUCCESS
            DepositEventType.FAIL -> BusinessProcessEvent.TPNET_DEPOSIT_FAIL
        }

        try {
            //send messageCorrelation for Process.POD_PAYMENT by businessKey = paymentTaskId
            camundaService.createMessageCorrelation(paymentTaskId, event)
        } catch (ex: Exception) {
            //send messageCorrelation to Process.SBERBANK_PREPAYMENT_WITH_TPNET by local variable PAYMENT_TASK_ID = paymentTaskId
            val processInstance = camundaService.getPrePaymentTpNetProcessByVariable(PAYMENT_TASK_ID, paymentTaskId)
            mdcService.propagateOrchestrationData(processInstance.processInstanceId, camundaService)

            val subscription = camundaService.getSubscriptionByProcessInstanceIdAndEventName(
                    processInstanceId = processInstance.processInstanceId,
                    eventName = when (depositEventType) {
                        DepositEventType.SUCCESS -> BusinessProcessEvent.TPNET_DEPOSIT_SUCCESS.message
                        DepositEventType.FAIL -> BusinessProcessEvent.TPNET_DEPOSIT_FAIL.message
                    }
            )

            camundaService.executeEvent(subscription.eventName, subscription.executionId)
        }
    }

    fun doGiveAway(paymentTaskId: String) {
        tpNetSender.sendGiveAwayCommand(paymentTaskId)
    }
}
