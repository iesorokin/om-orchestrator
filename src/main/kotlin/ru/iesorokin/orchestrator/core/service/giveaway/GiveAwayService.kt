package ru.iesorokin.payment.orchestrator.core.service.giveaway

import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.domain.GiveAway
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.TPNET_GIVE_AWAY_FAIL
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.BusinessProcessEvent.TPNET_GIVE_AWAY_SUCCESS
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process.PAYMENT_GIVEAWAY
import ru.iesorokin.payment.orchestrator.core.enums.bpmn.Process.TP_NET_INTERACTION
import ru.iesorokin.payment.orchestrator.core.exception.GiveAwaysNotFoundException
import ru.iesorokin.payment.orchestrator.core.service.CamundaService
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService

@Service
class GiveAwayService(
        private val paymentTaskService: PaymentTaskService,
        private val camundaService: CamundaService
) {
    fun processSuccessGiveAway(paymentTaskId: String) {
        processFirstActiveGiveAway(paymentTaskId, TPNET_GIVE_AWAY_SUCCESS)
    }

    fun processFailedGiveAway(paymentTaskId: String) {
        processFirstActiveGiveAway(paymentTaskId, TPNET_GIVE_AWAY_FAIL)
    }

    private fun processFirstActiveGiveAway(paymentTaskId: String, processEvent: BusinessProcessEvent) {
        val giveAways = paymentTaskService.getGiveAways(paymentTaskId)
        val firstActiveGiveAway = getFirstActiveGiveAway(giveAways)
                ?: throw GiveAwaysNotFoundException("Not found suitable give away in paymentTask " +
                        "with id:$paymentTaskId for processing:$processEvent.")
        if (firstActiveGiveAway.giveAwayId != null) {
            camundaService.createMessageCorrelation(
                    firstActiveGiveAway.giveAwayId,
                    processEvent
            )
        } else {
            camundaService.sendBusinessProcessMessage(
                    firstActiveGiveAway.processInstanceId!!,
                    processEvent
            )
        }
    }

    //@TODO remove findActiveInstanceById when on production will be no active giveAway with nullable businessKey
    private fun getFirstActiveGiveAway(giveAways: Collection<GiveAway>): GiveAway? {
        val sortedGiveAways = giveAways.sortedBy { it.created }
        return findActiveInstanceByBusinessKey(sortedGiveAways) ?: findActiveInstanceById(sortedGiveAways)
    }

    private fun findActiveInstanceByBusinessKey(sortedGiveAways: List<GiveAway>): GiveAway? {
        val activeGiveAwayProcessInstances = camundaService
                .getActiveProcessInstanceByBusinessKey(PAYMENT_GIVEAWAY)
        val activeTpNetInteractionProcessInstances = camundaService
                .getActiveProcessInstanceByBusinessKey(TP_NET_INTERACTION)
        return sortedGiveAways.firstOrNull {
            activeGiveAwayProcessInstances.contains(it.giveAwayId)
                    || activeTpNetInteractionProcessInstances.contains(it.giveAwayId)

        }
    }

    private fun findActiveInstanceById(sortedGiveAways: List<GiveAway>): GiveAway? {
        val activeGiveAwayProcessInstanceById = camundaService
                .getActiveProcessInstanceById(PAYMENT_GIVEAWAY)
        return sortedGiveAways.firstOrNull {
            activeGiveAwayProcessInstanceById.contains(it.processInstanceId)
        }
    }

}