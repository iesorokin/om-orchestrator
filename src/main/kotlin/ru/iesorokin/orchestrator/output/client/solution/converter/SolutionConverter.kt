package ru.iesorokin.orchestrator.output.client.solution.converter

import org.springframework.stereotype.Component
import ru.iesorokin.orchestrator.core.domain.CreationInformation
import ru.iesorokin.orchestrator.core.domain.LastUpdateInformation
import ru.iesorokin.orchestrator.core.domain.Phone
import ru.iesorokin.orchestrator.core.domain.Solution
import ru.iesorokin.orchestrator.core.domain.SolutionCustomer
import ru.iesorokin.orchestrator.core.domain.SolutionDiscount
import ru.iesorokin.orchestrator.core.domain.SolutionLine
import ru.iesorokin.orchestrator.output.client.dto.SolutionResponse
import ru.iesorokin.orchestrator.output.client.dto.SolutionResponseCreationInformation
import ru.iesorokin.orchestrator.output.client.dto.SolutionResponseCustomer
import ru.iesorokin.orchestrator.output.client.dto.SolutionResponseDiscount
import ru.iesorokin.orchestrator.output.client.dto.SolutionResponseLastUpdateInformation
import ru.iesorokin.orchestrator.output.client.dto.SolutionResponseLine
import ru.iesorokin.orchestrator.output.client.dto.SolutionResponsePhone

@Component
class SolutionConverter {

    fun convertSolutionResponseToSolution(response: SolutionResponse) = Solution(
            solutionId = response.solutionId,
            transactionId = response.transactionId,
            pinCode = response.pinCode,
            originStore = response.originStore,
            documentVersion = response.documentVersion,
            documentFormatVersion = response.documentFormatVersion,
            creationInformation = response.creationInformation?.toCreationInformation(),
            lastUpdateInformation = response.lastUpdateInformation?.toSolutionLastUpdateInformation(),
            solutionStatus = response.solutionStatus,
            workflowId = response.workflowId,
            solutionLines = response.solutionLines?.map { it.toSolutionLine() },
            customers = response.customers?.map { it.toCustomer() }
    )

    private fun SolutionResponseCreationInformation.toCreationInformation() = CreationInformation(
            createdBy = this.createdBy,
            created = this.created,
            channel = this.channel,
            deviceType = this.deviceType,
            frontApplication = this.frontApplication
    )

    private fun SolutionResponseLastUpdateInformation.toSolutionLastUpdateInformation() = LastUpdateInformation(
            updateBy = this.updateBy,
            updated = this.updated
    )

    private fun SolutionResponseLine.toSolutionLine() = SolutionLine(
            itemReference = this.itemReference,
            lineId = this.lineId,
            longTail = this.longTail,
            stock = this.stock,
            vat = this.vat,
            confirmedQuantity = this.confirmedQuantity,
            type = this.type,
            price = this.price,
            quantity = this.quantity,
            name = this.name,
            discount = this.discount?.toSolutionDiscount()
    )

    private fun SolutionResponseDiscount.toSolutionDiscount() = SolutionDiscount(
            type = this.type,
            reason = this.reason,
            typeValue = this.typeValue,
            actor = this.actor
    )

    private fun SolutionResponseCustomer.toCustomer() = SolutionCustomer(
            customerNumber = this.customerNumber,
            name = this.name,
            surname = this.surname,
            phone = this.phone?.toPhone(),
            email = this.email,
            type = this.type,
            roles = this.roles
    )

    private fun SolutionResponsePhone.toPhone() = Phone(
            primary = this.primary,
            secondary = this.secondary
    )

}
