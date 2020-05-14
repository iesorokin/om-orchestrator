package ru.iesorokin.payment.orchestrator.core.service.refund

import org.springframework.stereotype.Service
import ru.iesorokin.payment.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.payment.orchestrator.output.stream.sender.ConductorSender
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.conductor.RefundMessage
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.conductor.RefundMessageLine
import ru.iesorokin.payment.orchestrator.output.stream.sender.dto.conductor.RefundType

@Service
class RefundService(private val paymentTaskService: PaymentTaskService,
                    private val refundContextService: RefundContextService,
                    private val conductorSender: ConductorSender) {

    fun startRefundProcess(paymentTaskId: String) {
        val paymentTask = paymentTaskService.getPaymentTask(paymentTaskId)

        val refundLines = refundContextService.buildRefundLines(paymentTask).map {
            RefundMessageLine(
                    extLineId = it.extLineId,
                    quantity = it.quantity,
                    unitAmountIncludingVat = it.unitAmountIncludingVat
            )
        }

        if (refundLines.isNotEmpty()) {
            conductorSender.sendRefundMessage(
                    RefundMessage(
                            refundType = RefundType.PARTIAL,
                            extOrderId = paymentTask.extOrderId!!,
                            paymentTaskId = paymentTaskId,
                            currentPaymentStatus = paymentTask.taskStatus,
                            taskType = paymentTask.taskType,
                            lines = refundLines
                    )
            )
        }
    }

}
