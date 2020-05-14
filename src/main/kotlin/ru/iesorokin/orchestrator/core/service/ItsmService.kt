package ru.iesorokin.payment.orchestrator.core.service

import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.domain.TpnetItsmTicket
import ru.iesorokin.payment.orchestrator.output.stream.sender.ItsmSender

@Service
class ItsmService(private val itsmSender: ItsmSender) {

    fun createTicket(tpnetItsmTicket: TpnetItsmTicket) {
        itsmSender.sendTpnetItsmTicketMessage(tpnetItsmTicket)
    }

}
