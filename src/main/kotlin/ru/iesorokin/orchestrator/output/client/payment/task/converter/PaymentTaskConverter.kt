package ru.iesorokin.payment.orchestrator.output.client.payment.task.converter

import org.springframework.stereotype.Component
import ru.iesorokin.payment.orchestrator.core.domain.AgencyAgreement
import ru.iesorokin.payment.orchestrator.core.domain.CreationInformation
import ru.iesorokin.payment.orchestrator.core.domain.EditLine
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTask
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskFiscalData
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskFiscalDataLine
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskLine
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskLineDiscount
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskLink
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskRegisterStatus
import ru.iesorokin.payment.orchestrator.core.domain.PaymentTaskRegisterStatusLine
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponse
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponseCreated
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponseFiscalData
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponseFiscalDataLine
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponseLine
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponseLineDiscount
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponseLink
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponseRegisterStatus
import ru.iesorokin.payment.orchestrator.output.client.dto.PaymentTaskResponseRegisterStatusLine
import ru.iesorokin.payment.orchestrator.output.client.payment.task.dto.PatchLineRequest

@Component
class PaymentTaskConverter {

    fun convertPaymentTaskResponseToPaymentTask(response: PaymentTaskResponse) = PaymentTask(
            buCode = response.buCode,
            channel = response.channel,
            taskId = response.taskId,
            taskStatus = response.taskStatus,
            extOrderId = response.extOrderId,
            workflowId = response.workflowId,
            creationInformation = response.creationInformation?.toCreationInformation(),
            taskType = response.taskType,
            executionStore = response.executionStore,
            links = response.links?.map { it.toPaymentTaskLink() },
            pinCode = response.pinCode,
            lines = response.lines.map { it.toPaymentTaskLine() },
            registerStatus = response.registerStatus?.toPaymentTaskRegisterStatus(),
            refundStatusList = response.refundStatusList?.map { it.toFiscalData() },
            extLineIdToAgencyAgreement = response.agencyAgreements?.groupExtLineIdToAgencyAgreement()
    )

    fun convertEditLinesToPathLinesRequest(editLines: Collection<EditLine>) =
            editLines.map {
                PatchLineRequest(
                        extLineId = it.extLineId,
                        unitAmountIncludingVat = it.unitAmountIncludingVat,
                        confirmedQuantity = it.confirmedQuantity
                )
            }

    private fun PaymentTaskResponseCreated.toCreationInformation() = CreationInformation(
            createdBy = this.createdBy,
            created = this.created
    )

    private fun PaymentTaskResponseLink.toPaymentTaskLink() = PaymentTaskLink(
            type = this.type,
            link = this.link
    )

    private fun PaymentTaskResponseLine.toPaymentTaskLine() = PaymentTaskLine(
            lineStatus = this.lineStatus,
            itemCode = this.itemCode,
            lineType = this.lineType,
            itemName = this.itemName,
            quantity = this.quantity,
            unitAmountIncludingVat = this.unitAmountIncludingVat,
            taxRate = this.taxRate,
            unitVatAmount = this.unitVatAmount,
            extLineId = this.extLineId,
            confirmedQuantity = this.confirmedQuantity,
            depositQuantity = this.depositQuantity,
            discount = this.discount?.toPaymentTaskLineDiscount()
    )

    private fun PaymentTaskResponseRegisterStatus.toPaymentTaskRegisterStatus() = PaymentTaskRegisterStatus(
            atolId = this.atolId,
            status = this.status,
            uuid = this.uuid,
            ecrRegistrationNumber = this.ecrRegistrationNumber,
            fiscalDocumentNumber = this.fiscalDocumentNumber,
            fiscalStorageNumber = this.fiscalStorageNumber,
            lines = this.lines?.map { it.toPaymentTaskRegisterStatusLine() }
    )

    private fun PaymentTaskResponseRegisterStatusLine.toPaymentTaskRegisterStatusLine() = PaymentTaskRegisterStatusLine(
            extLineId = this.extLineId,
            quantity = this.quantity,
            unitAmountIncludingVat = this.unitAmountIncludingVat
    )

    private fun PaymentTaskResponseFiscalData.toFiscalData() =
            PaymentTaskFiscalData(
                    created = this.created,
                    atolId = this.atolId,
                    uuid = this.uuid,
                    ecrRegistrationNumber = this.ecrRegistrationNumber,
                    fiscalDocumentNumber = this.fiscalDocumentNumber,
                    fiscalStorageNumber = this.fiscalStorageNumber,
                    status = this.status,
                    refundWorkflowId = this.refundWorkflowId,
                    lines = this.lines?.map { it.toTaskFiscalLineResponse() }
            )

    private fun PaymentTaskResponseFiscalDataLine.toTaskFiscalLineResponse() =
            PaymentTaskFiscalDataLine(
                    extLineId = this.extLineId,
                    lineId = this.lineId,
                    quantity = this.quantity,
                    unitAmountIncludingVat = this.unitAmountIncludingVat
            )

    private fun PaymentTaskResponseLineDiscount.toPaymentTaskLineDiscount() = PaymentTaskLineDiscount(
            type = this.type,
            typeValue = this.typeValue
    )

    private fun Collection<AgencyAgreement>.groupExtLineIdToAgencyAgreement(): Map<String, AgencyAgreement> {
        val mutableMap = mutableMapOf<String, AgencyAgreement>()

        this.forEach { it.extLineIds?.associateTo(mutableMap) { id -> id to it } }

        return mutableMap
    }
}
