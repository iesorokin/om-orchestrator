package ru.iesorokin.payment.orchestrator.web.converter

import org.springframework.stereotype.Component
import ru.iesorokin.payment.orchestrator.core.domain.EditLine
import ru.iesorokin.payment.orchestrator.core.domain.GiveAwayLine
import ru.iesorokin.payment.orchestrator.web.dto.EditLineRequest
import ru.iesorokin.payment.orchestrator.web.dto.GiveAwayLineRequest

@Component
class DtoConverter {

    fun convertEditLineRequestToEditLine(editLineRequest: Collection<EditLineRequest>) =
            editLineRequest
                    .map { EditLine(
                            extLineId = it.extLineId,
                            unitAmountIncludingVat = it.unitAmountIncludingVat,
                            confirmedQuantity = it.confirmedQuantity)
                    }

    fun convertGiveAwayLinesRequestToGiveAwayLinesInput(giveAwayRequest: Collection<GiveAwayLineRequest>) =
            giveAwayRequest
                    .map { GiveAwayLine(
                            extLineId = it.extLineId,
                            quantity = it.quantity
                    ) }
}