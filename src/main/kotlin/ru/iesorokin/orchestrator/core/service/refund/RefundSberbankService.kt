package ru.iesorokin.orchestrator.core.service.refund

import org.springframework.stereotype.Service
import ru.iesorokin.orchestrator.config.jackson.MONEY_SCALE
import ru.iesorokin.orchestrator.core.domain.PaymentTask
import ru.iesorokin.orchestrator.core.domain.PaymentTaskFiscalDataLine
import ru.iesorokin.orchestrator.core.exception.SberbankRefundException
import ru.iesorokin.orchestrator.core.service.PaymentTaskService
import ru.iesorokin.orchestrator.output.client.payment.SberbankClient
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class RefundSberbankService(private val paymentTaskService: PaymentTaskService,
                            private val sberbankClient: SberbankClient) {

    fun doRefund(workflowId: String, paymentTaskId: String, orderId: String) {
        val paymentTask = getPaymentTask(paymentTaskId)
        val storeId = paymentTask.executionStore
                ?: throw SberbankRefundException("ExecutionStore is null. For paymentTaskId: $paymentTaskId")

        val refundList = paymentTask.refundStatusList
                ?.firstOrNull { workflowId == it.refundWorkflowId }
                ?.lines
                ?: throw SberbankRefundException("Not found refundList with refundWorkflowId: $workflowId")
        val refundAmount = calculateRefundAmount(refundList)

        refund(orderId, storeId, refundAmount)
    }

    private fun getPaymentTask(paymentTaskId: String): PaymentTask = try {
        paymentTaskService.getPaymentTask(paymentTaskId)
    } catch (e: Exception) {
        throw SberbankRefundException("Error while getting paymentTask with paymentTaskId id $paymentTaskId")
    }

    private fun refund(orderId: String, storeId: Int, refundAmount: BigDecimal) = try {
        sberbankClient.refund(orderId, storeId, refundAmount)
    } catch (e: Exception) {
        throw SberbankRefundException("Error while send refund orderId: $orderId, storeId: $storeId")
    }

    private fun calculateRefundAmount(refundList: Collection<PaymentTaskFiscalDataLine>): BigDecimal =
            refundList
                    .fold(BigDecimal.ZERO) { acc, taskFiscalDataLine ->
                        acc + taskFiscalDataLine.quantity
                                .multiply(taskFiscalDataLine.unitAmountIncludingVat
                                        ?: throw SberbankRefundException("Not found unitAmountIncludingVat for extLineId: ${taskFiscalDataLine.extLineId}"))
                                .setScale(MONEY_SCALE, RoundingMode.HALF_UP)
                    }
}
