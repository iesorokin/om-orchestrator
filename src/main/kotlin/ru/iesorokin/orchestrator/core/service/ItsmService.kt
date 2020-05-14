package ru.iesorokin.orchestrator.core.service

import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.core.domain.TpnetItsmTicket
import ru.iesorokin.orchestrator.output.stream.sender.ItsmSender

@Service
class ItsmService(private val itsmSender: ItsmSender) {

    fun createTicket(tpnetItsmTicket: TpnetItsmTicket) {
        itsmSender.sendTpnetItsmTicketMessage(tpnetItsmTicket)
    }

}
