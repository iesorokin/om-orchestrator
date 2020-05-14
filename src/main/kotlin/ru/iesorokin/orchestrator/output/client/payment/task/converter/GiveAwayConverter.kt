package ru.iesorokin.payment.orchestrator.output.client.payment.task.converter

import org.springframework.stereotype.Component
import ru.iesorokin.payment.orchestrator.core.domain.AgencyAgreement
import ru.iesorokin.payment.orchestrator.core.domain.GiveAway
import ru.iesorokin.payment.orchestrator.core.domain.GiveAwayExternalLine
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.AddGiveAwayLineRequest
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.AddGiveAwayRequest
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.AgencyAgreementResponse
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.GiveAwayResponseItem
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.GiveAwayResponseItemLine

@Component
class GiveAwayConverter {
    fun toGiveAways(giveAwayResponseItems: Collection<GiveAwayResponseItem>): Collection<GiveAway> =
            giveAwayResponseItems.map { it.toGiveAway() }

    fun toGiveAwayLinesRequest(lines: Collection<GiveAwayExternalLine>) =
            lines.map {
                AddGiveAwayLineRequest(
                        extLineId = it.extLineId,
                        itemCode = it.itemCode,
                        unitAmountIncludingVat = it.unitAmountIncludingVat,
                        quantity = it.quantity,
                        agencyAgreement = it.agencyAgreement)
            }

    fun toAddGiveAwayRequest(giveAway: GiveAway) =
            AddGiveAwayRequest(
                    giveAwayId = giveAway.giveAwayId!!,
                    createdBy = giveAway.createdBy,
                    lines = toGiveAwayLinesRequest(giveAway.lines))

    private fun GiveAwayResponseItem.toGiveAway(): GiveAway =
            GiveAway(
                    giveAwayId = giveAwayId,
                    createdBy = createdBy,
                    created = created,
                    processInstanceId = processInstanceId,
                    lines = lines.map { it.toGiveAwayExternalLine() }
            )

    private fun GiveAwayResponseItemLine.toGiveAwayExternalLine(): GiveAwayExternalLine =
            GiveAwayExternalLine(
                    extLineId = extLineId,
                    itemCode = itemCode,
                    unitAmountIncludingVat = unitAmountIncludingVat,
                    lineId = lineId,
                    quantity = quantity,
                    agencyAgreement = agencyAgreement?.toAgencyAgreement()
            )

    private fun AgencyAgreementResponse.toAgencyAgreement(): AgencyAgreement =
            AgencyAgreement(
                    supplierName = supplierName,
                    supplierInn = supplierInn,
                    supplierType = supplierType,
                    supplierTaxRate = supplierTaxRate,
                    supplierPhones = supplierPhone
            )
}
