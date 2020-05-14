package ru.iesorokin.orchestrator.output.client.payment.task.converter

import org.springframework.stereotype.Component
import ru.iesorokin.orchestrator.core.domain.RefundContext
import ru.iesorokin.orchestrator.core.domain.RefundLine
import ru.iesorokin.orchestrator.output.client.dto.JsonPatchRequestOperation
import ru.iesorokin.orchestrator.output.client.dto.OperationType
import ru.iesorokin.orchestrator.output.client.payment.task.dto.PatchRefundLine
import ru.iesorokin.orchestrator.output.client.payment.task.dto.PatchRefundRequest
import ru.iesorokin.orchestrator.output.client.payment.task.dto.RefundStatus

const val PATCH_PATH_REFUND_STATUS_LIST = "/refundStatusList/-"
const val PAYMENT_STATUS_APPROVE_IN_PROGRESS = "APPROVE_IN_PROGRESS"
const val PAYMENT_STATUS_COMPLETED = "COMPLETED"

@Component
class RefundDtosConverter {
    fun toRequest(processInstanceId: String, refundContext: RefundContext): Collection<JsonPatchRequestOperation> =
            listOf(JsonPatchRequestOperation(
                    OperationType.ADD,
                    PATCH_PATH_REFUND_STATUS_LIST,
                    refundContext.toRequestValue(processInstanceId))
            )
}

private fun RefundContext.toRequestValue(processInstanceId: String): PatchRefundRequest {
    return PatchRefundRequest(
            status = currentPaymentStatus.toPatchStatus(),
            refundWorkflowId = processInstanceId,
            lines = lines
                    .map { it.toPatchRefundLine() }
                    .toList()
    )
}

private fun RefundLine.toPatchRefundLine(): PatchRefundLine {
    return PatchRefundLine(
            extLineId = extLineId,
            quantity = quantity,
            unitAmountIncludingVat = unitAmountIncludingVat
    )
}

private fun String.toPatchStatus(): String =
        when (this) {
            PAYMENT_STATUS_APPROVE_IN_PROGRESS -> RefundStatus.UNHOLD.name
            PAYMENT_STATUS_COMPLETED -> RefundStatus.REFUND_AFTER_GIVEAWAY.name
            else -> RefundStatus.REFUND.name
        }
